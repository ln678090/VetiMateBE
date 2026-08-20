
package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.AppointmentStatus;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.ExaminationHistoryResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.MedicalRecordResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.MedicineOptionResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.PrescriptionItemRequest;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.PrescriptionResponse;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.ReplacePrescriptionsRequest;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.SaveExaminationRequest;
import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;
import com.graduation.project.clinic.examination.entity.Prescription;
import com.graduation.project.clinic.examination.exception.ClinicWorkflowException;
import com.graduation.project.clinic.examination.repository.MedicalRecordRepository;
import com.graduation.project.clinic.examination.repository.PrescriptionRepository;
import com.graduation.project.clinic.repository.AppointmentRepository;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.inventory.repository.MedicineRepository;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExaminationService {

  private static final String DOCTOR_ROLE_TYPE = "DOCTOR";

  private final AppointmentRepository appointmentRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final StaffRepository staffRepository;
  private final MedicineRepository medicineRepository;

  @Transactional
  public MedicalRecordResponse openExamination(
      UUID appointmentId,
      UUID currentUserId) {
    Staff doctor = requireActiveDoctor(currentUserId);

    Appointment appointment = appointmentRepository
        .findByIdForUpdate(appointmentId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Không tìm thấy lịch hẹn"));

    if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new ClinicWorkflowException(
          "Chỉ được khám lịch ở trạng thái CONFIRMED");
    }

    MedicalRecord medicalRecord = medicalRecordRepository
        .findByAppointment_Id(appointmentId)
        .orElseGet(() -> createMedicalRecord(
            appointment,
            doctor));

    if (!medicalRecord.getDoctor().getId()
        .equals(doctor.getId())) {
      throw new ClinicWorkflowException(
          "Phiếu khám đã được bác sĩ khác tiếp nhận");
    }

    return toResponse(medicalRecord);
  }

  @Transactional(readOnly = true)
  public MedicalRecordResponse getById(
      UUID medicalRecordId,
      UUID currentUserId) {
    return toResponse(requireOwnedRecord(
        medicalRecordId,
        currentUserId));
  }

  @Transactional
  public MedicalRecordResponse saveExamination(
      UUID medicalRecordId,
      SaveExaminationRequest request,
      UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecord(
        medicalRecordId,
        currentUserId);

    requireInProgress(medicalRecord);

    medicalRecord.setSymptoms(
        normalize(request.symptoms()));
    medicalRecord.setDiagnosis(
        request.diagnosis().trim());
    medicalRecord.setTreatmentPlan(
        normalize(request.treatmentPlan()));
    medicalRecord.setWeightKg(request.weightKg());
    medicalRecord.setDoctorNote(
        normalize(request.doctorNote()));

    medicalRecordRepository.save(medicalRecord);

    return toResponse(medicalRecord);
  }

  @Transactional
  public MedicalRecordResponse replacePrescriptions(
      UUID medicalRecordId,
      ReplacePrescriptionsRequest request,
      UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecord(
        medicalRecordId,
        currentUserId);

    requireInProgress(medicalRecord);

    Set<UUID> medicineIds = request.items()
        .stream()
        .map(PrescriptionItemRequest::medicineId)
        .collect(Collectors.toSet());

    Map<UUID, Medicine> medicineById = medicineRepository
        .findAllByIdInAndIsActiveTrue(medicineIds)
        .stream()
        .collect(Collectors.toMap(
            Medicine::getId,
            Function.identity()));

    if (medicineById.size() != medicineIds.size()) {
      throw new ClinicWorkflowException(
          "Có thuốc không tồn tại hoặc đã ngừng sử dụng");
    }

    prescriptionRepository.deleteAllByMedicalRecord_Id(
        medicalRecordId);
    prescriptionRepository.flush();

    List<Prescription> prescriptions = request.items()
        .stream()
        .map(item -> createPrescription(
            medicalRecord,
            medicineById.get(item.medicineId()),
            item))
        .toList();

    prescriptionRepository.saveAll(prescriptions);

    return toResponse(medicalRecord);
  }

  @Transactional
  public MedicalRecordResponse complete(
      UUID medicalRecordId,
      UUID currentUserId) {
    MedicalRecord medicalRecord = requireOwnedRecord(
        medicalRecordId,
        currentUserId);

    requireInProgress(medicalRecord);

    if (medicalRecord.getDiagnosis() == null
        || medicalRecord.getDiagnosis().isBlank()) {
      throw new ClinicWorkflowException(
          "Phải nhập chẩn đoán trước khi hoàn tất");
    }

    // if (prescriptionRepository.countByMedicalRecord_Id(
    // medicalRecordId) == 0) {
    // throw new ClinicWorkflowException(
    // "Phải kê ít nhất một thuốc trước khi hoàn tất");
    // }

    Appointment appointment = medicalRecord.getAppointment();

    if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
      throw new ClinicWorkflowException(
          "Lịch hẹn không còn ở trạng thái CONFIRMED");
    }

    medicalRecord.setStatus(
        MedicalRecordStatus.COMPLETED);
    appointment.setStatus(AppointmentStatus.DONE);

    medicalRecordRepository.save(medicalRecord);
    appointmentRepository.save(appointment);

    return toResponse(medicalRecord);
  }

  @Transactional(readOnly = true)
  public List<MedicineOptionResponse> getMedicines() {
    return medicineRepository
        .findByIsActiveTrueOrderByNameAsc()
        .stream()
        .map(medicine -> new MedicineOptionResponse(
            medicine.getId(),
            medicine.getName(),
            medicine.getSku(),
            medicine.getUnit(),
            medicine.getSellPrice()))
        .toList();
  }

  private Staff requireActiveDoctor(UUID currentUserId) {
    return staffRepository
        .findByUserIdAndRoleTypeAndActiveTrue(
            currentUserId,
            DOCTOR_ROLE_TYPE)
        .orElseThrow(() -> new ClinicWorkflowException(
            "Tài khoản chưa liên kết với hồ sơ bác sĩ"));
  }

  private MedicalRecord requireOwnedRecord(
      UUID medicalRecordId,
      UUID currentUserId) {
    Staff doctor = requireActiveDoctor(currentUserId);

    MedicalRecord medicalRecord = medicalRecordRepository
        .findDetailedById(medicalRecordId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Không tìm thấy phiếu khám"));

    if (!medicalRecord.getDoctor().getId()
        .equals(doctor.getId())) {
      throw new ClinicWorkflowException(
          "Bạn không phải bác sĩ phụ trách phiếu khám");
    }

    return medicalRecord;
  }

  private MedicalRecord createMedicalRecord(
      Appointment appointment,
      Staff doctor) {
    MedicalRecord medicalRecord = new MedicalRecord();

    medicalRecord.setAppointment(appointment);
    medicalRecord.setPet(appointment.getPet());
    medicalRecord.setDoctor(doctor);
    medicalRecord.setStatus(
        MedicalRecordStatus.IN_PROGRESS);

    return medicalRecordRepository.save(medicalRecord);
  }

  private Prescription createPrescription(
      MedicalRecord medicalRecord,
      Medicine medicine,
      PrescriptionItemRequest request) {
    Prescription prescription = new Prescription();

    prescription.setMedicalRecord(medicalRecord);
    prescription.setMedicine(medicine);
    prescription.setQuantity(request.quantity());
    prescription.setDosage(request.dosage().trim());
    prescription.setDurationDays(
        request.durationDays());
    prescription.setNote(normalize(request.note()));

    return prescription;
  }

  private void requireInProgress(
      MedicalRecord medicalRecord) {
    if (medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS) {
      throw new ClinicWorkflowException(
          "Phiếu khám đã hoàn tất và không thể chỉnh sửa");
    }
  }

  private MedicalRecordResponse toResponse(
      MedicalRecord medicalRecord) {
    List<PrescriptionResponse> prescriptions = prescriptionRepository
        .findAllByMedicalRecord_IdOrderByIdAsc(
            medicalRecord.getId())
        .stream()
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
        medicalRecord.getDoctorNote(),
        medicalRecord.getStatus().name(),
        medicalRecord.getCreatedAt(),
        medicalRecord.getUpdatedAt(),
        prescriptions);
  }

  @Transactional(readOnly = true)
  public Page<ExaminationHistoryResponse> getHistory(
      UUID currentUserId,
      Pageable pageable) {
    return medicalRecordRepository
        .findByDoctor_UserIdAndStatus(
            currentUserId,
            MedicalRecordStatus.COMPLETED,
            pageable)
        .map(record -> new ExaminationHistoryResponse(
            record.getId(),
            record.getAppointment().getId(),
            record.getAppointment().getPet().getId(),
            record.getAppointment().getPet().getName(),
            record.getDiagnosis(),
            record.getStatus(),
            record.getUpdatedAt()));
  }

  private PrescriptionResponse toPrescriptionResponse(
      Prescription prescription) {
    Medicine medicine = prescription.getMedicine();

    return new PrescriptionResponse(
        prescription.getId(),
        medicine.getId(),
        medicine.getName(),
        medicine.getUnit(),
        prescription.getQuantity(),
        prescription.getDosage(),
        prescription.getDurationDays(),
        prescription.getNote());
  }

  private String normalize(String value) {
    return value == null || value.isBlank()
        ? null
        : value.trim();
  }
}
