package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.AppointmentStatus;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.enums.PetHealthStatus;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.ExaminationHistoryResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.MedicalRecordResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.MedicineOptionResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.PrescriptionItemRequest;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.PrescriptionItemResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.ReplacePrescriptionsRequest;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.SaveExaminationRequest;
import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;
import com.graduation.project.clinic.examination.entity.Prescription;
import com.graduation.project.clinic.examination.exception.ClinicWorkflowException;
import com.graduation.project.clinic.examination.repository.MedicalRecordRepository;
import com.graduation.project.clinic.examination.repository.PrescriptionRepository;
import com.graduation.project.clinic.repository.AppointmentRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.inventory.repository.MedicineRepository;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.entity.StaffRoleType;
import com.graduation.project.staff.repository.StaffRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExaminationService {

  // private static final String DOCTOR_ROLE_TYPE = "DOCTOR";

  private final AppointmentRepository appointmentRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final StaffRepository staffRepository;
  private final MedicineRepository medicineRepository;
  private final PetRepository petRepository;

  @Transactional
  public MedicalRecordResponse openExamination(UUID appointmentId, UUID currentUserId) {
    Staff doctor = requireActiveDoctor(currentUserId);

    Appointment appointment =
        appointmentRepository
            .findByIdForUpdate(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn"));

    if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new ClinicWorkflowException("Chỉ được khám lịch ở trạng thái CONFIRMED");
    }

    MedicalRecord medicalRecord =
        medicalRecordRepository
            .findByAppointment_Id(appointmentId)
            .orElseGet(() -> createMedicalRecord(appointment, doctor));

    requireDoctorOwnership(medicalRecord, doctor);

    return toResponse(medicalRecord);
  }

  public MedicalRecordResponse getById(UUID medicalRecordId, UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecord(medicalRecordId, currentUserId);

    return toResponse(medicalRecord);
  }

  @Transactional
  public MedicalRecordResponse saveExamination(
      UUID medicalRecordId, SaveExaminationRequest request, UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecordForUpdate(medicalRecordId, currentUserId);

    requireInProgress(medicalRecord);

    medicalRecord.setSymptoms(normalize(request.symptoms()));
    medicalRecord.setDiagnosis(normalize(request.diagnosis()));
    medicalRecord.setTreatmentPlan(normalize(request.treatmentPlan()));
    medicalRecord.setWeightKg(request.weightKg());
    medicalRecord.setHealthStatus(request.healthStatus());
    medicalRecord.setDoctorNote(normalize(request.doctorNote()));

    MedicalRecord savedRecord = medicalRecordRepository.saveAndFlush(medicalRecord);

    /*
     * Không cập nhật Pet ở đây.
     * Bệnh án vẫn chỉ là bản nháp IN_PROGRESS.
     */
    return toResponse(savedRecord);
  }

  @Transactional
  public MedicalRecordResponse replacePrescriptions(
      UUID medicalRecordId, ReplacePrescriptionsRequest request, UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecordForUpdate(medicalRecordId, currentUserId);

    requireInProgress(medicalRecord);

    List<PrescriptionItemRequest> requestedItems = request.items();

    Set<UUID> medicineIds =
        requestedItems.stream()
            .map(PrescriptionItemRequest::medicineId)
            .collect(Collectors.toSet());

    if (medicineIds.size() != requestedItems.size()) {
      throw new ClinicWorkflowException("Một loại thuốc không được xuất hiện nhiều lần trong đơn");
    }

    Map<UUID, Medicine> medicineById =
        medicineRepository.findAllByIdInAndIsActiveTrue(medicineIds).stream()
            .collect(Collectors.toMap(Medicine::getId, Function.identity()));

    if (medicineById.size() != medicineIds.size()) {
      throw new ClinicWorkflowException("Có thuốc không tồn tại hoặc đã ngừng sử dụng");
    }

    prescriptionRepository.deleteAllByMedicalRecord_Id(medicalRecordId);
    prescriptionRepository.flush();

    List<Prescription> prescriptions =
        requestedItems.stream()
            .map(
                requestItem ->
                    createPrescription(
                        medicalRecord, medicineById.get(requestItem.medicineId()), requestItem))
            .toList();

    if (!prescriptions.isEmpty()) {
      prescriptionRepository.saveAllAndFlush(prescriptions);
    }

    return toResponse(medicalRecord);
  }

  @Transactional
  public MedicalRecordResponse complete(UUID medicalRecordId, UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecordForUpdate(medicalRecordId, currentUserId);

    requireInProgress(medicalRecord);
    validateBeforeCompletion(medicalRecord);

    Appointment appointment = medicalRecord.getAppointment();

    if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new ClinicWorkflowException("Lịch hẹn không còn ở trạng thái CONFIRMED");
    }

    Pet pet =
        petRepository
            .findByIdForUpdate(medicalRecord.getPet().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thú cưng"));

    Instant completedAt = Instant.now();

    medicalRecord.setStatus(MedicalRecordStatus.COMPLETED);
    appointment.setStatus(AppointmentStatus.DONE);

    pet.updateHealthSnapshot(
        medicalRecord.getHealthStatus(),
        medicalRecord.getWeightKg(),
        buildCurrentHealthNote(medicalRecord),
        completedAt);

    petRepository.save(pet);
    appointmentRepository.save(appointment);

    MedicalRecord savedRecord = medicalRecordRepository.saveAndFlush(medicalRecord);

    return toResponse(savedRecord);
  }

  public List<MedicineOptionResponse> getMedicines() {
    return medicineRepository.findByIsActiveTrueOrderByNameAsc().stream()
        .map(
            medicine ->
                new MedicineOptionResponse(
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getSku(),
                    medicine.getUnit(),
                    medicine.getSellPrice()))
        .toList();
  }

  public Page<ExaminationHistoryResponse> getHistory(UUID currentUserId, Pageable pageable) {
    requireActiveDoctor(currentUserId);

    return medicalRecordRepository
        .findByDoctor_UserIdAndStatus(currentUserId, MedicalRecordStatus.COMPLETED, pageable)
        .map(
            record ->
                new ExaminationHistoryResponse(
                    record.getId(),
                    record.getAppointment().getId(),
                    record.getPet().getId(),
                    record.getPet().getName(),
                    record.getDiagnosis(),
                    record.getHealthStatus(),
                    record.getWeightKg(),
                    record.getUpdatedAt()));
  }

  private Staff requireActiveDoctor(UUID currentUserId) {
    return staffRepository
        .findByUserIdAndRoleTypeAndActiveTrue(currentUserId, StaffRoleType.DOCTOR)
        .orElseThrow(() -> new ClinicWorkflowException("Tài khoản chưa liên kết với hồ sơ bác sĩ"));
  }

  private MedicalRecord requireOwnedRecord(UUID medicalRecordId, UUID currentUserId) {
    Staff doctor = requireActiveDoctor(currentUserId);

    MedicalRecord medicalRecord =
        medicalRecordRepository
            .findDetailedById(medicalRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám"));

    requireDoctorOwnership(medicalRecord, doctor);

    return medicalRecord;
  }

  private MedicalRecord requireOwnedRecordForUpdate(UUID medicalRecordId, UUID currentUserId) {
    Staff doctor = requireActiveDoctor(currentUserId);

    MedicalRecord medicalRecord =
        medicalRecordRepository
            .findDetailedByIdForUpdate(medicalRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu khám"));

    requireDoctorOwnership(medicalRecord, doctor);

    return medicalRecord;
  }

  private void requireDoctorOwnership(MedicalRecord medicalRecord, Staff doctor) {
    if (!medicalRecord.getDoctor().getId().equals(doctor.getId())) {
      throw new ClinicWorkflowException("Bạn không phải bác sĩ phụ trách phiếu khám");
    }
  }

  private MedicalRecord createMedicalRecord(Appointment appointment, Staff doctor) {
    MedicalRecord medicalRecord = new MedicalRecord();

    medicalRecord.setAppointment(appointment);
    medicalRecord.setPet(appointment.getPet());
    medicalRecord.setDoctor(doctor);
    medicalRecord.setHealthStatus(PetHealthStatus.MONITORING);
    medicalRecord.setStatus(MedicalRecordStatus.IN_PROGRESS);

    return medicalRecordRepository.saveAndFlush(medicalRecord);
  }

  private Prescription createPrescription(
      MedicalRecord medicalRecord, Medicine medicine, PrescriptionItemRequest request) {
    Prescription prescription = new Prescription();

    prescription.setMedicalRecord(medicalRecord);
    prescription.setMedicine(medicine);
    prescription.setQuantity(request.quantity());
    prescription.setDosage(request.dosage().trim());
    prescription.setDurationDays(request.durationDays());
    prescription.setNote(normalize(request.note()));

    return prescription;
  }

  private void requireInProgress(MedicalRecord medicalRecord) {
    if (medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS) {
      throw new ClinicWorkflowException("Phiếu khám đã hoàn tất và không thể chỉnh sửa");
    }
  }

  private void validateBeforeCompletion(MedicalRecord medicalRecord) {
    if (!hasText(medicalRecord.getDiagnosis())) {
      throw new ClinicWorkflowException("Phải nhập chẩn đoán trước khi hoàn tất");
    }

    if (medicalRecord.getHealthStatus() == null) {
      throw new ClinicWorkflowException("Phải đánh giá tình trạng sức khỏe");
    }

    BigDecimal weightKg = medicalRecord.getWeightKg();

    if (weightKg != null && weightKg.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ClinicWorkflowException("Cân nặng phải lớn hơn 0");
    }
  }

  private MedicalRecordResponse toResponse(MedicalRecord medicalRecord) {
    List<PrescriptionItemResponse> prescriptions =
        prescriptionRepository.findAllByMedicalRecord_IdOrderByIdAsc(medicalRecord.getId()).stream()
            .map(this::toPrescriptionResponse)
            .toList();

    return new MedicalRecordResponse(
        medicalRecord.getId(),
        medicalRecord.getAppointment().getId(),
        medicalRecord.getPet().getId(),
        medicalRecord.getDoctor().getId(),
        medicalRecord.getSymptoms(),
        medicalRecord.getDiagnosis(),
        medicalRecord.getTreatmentPlan(),
        medicalRecord.getWeightKg(),
        medicalRecord.getHealthStatus(),
        medicalRecord.getDoctorNote(),
        medicalRecord.getStatus(),
        medicalRecord.getCreatedAt(),
        medicalRecord.getUpdatedAt(),
        prescriptions);
  }

  private PrescriptionItemResponse toPrescriptionResponse(Prescription prescription) {
    Medicine medicine = prescription.getMedicine();

    return new PrescriptionItemResponse(
        prescription.getId(),
        medicine.getId(),
        medicine.getName(),
        medicine.getSku(),
        medicine.getUnit(),
        prescription.getQuantity(),
        prescription.getDosage(),
        prescription.getDurationDays(),
        prescription.getNote());
  }

  private String buildCurrentHealthNote(MedicalRecord medicalRecord) {
    if (hasText(medicalRecord.getDoctorNote())) {
      return medicalRecord.getDoctorNote().trim();
    }

    if (hasText(medicalRecord.getDiagnosis())) {
      return medicalRecord.getDiagnosis().trim();
    }

    return null;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();

    return normalized.isEmpty() ? null : normalized;
  }
}
