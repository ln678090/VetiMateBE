package com.graduation.project.clinic.examination.service.impl;

import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;
import com.graduation.project.clinic.examination.repository.MedicalRecordRepository;
import com.graduation.project.clinic.examination.service.OwnerPetHistoryService;
import com.graduation.project.clinic.repository.AppointmentRepository;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerPetHistoryServiceImpl implements OwnerPetHistoryService {

  private static final int MAX_STATUS_ITEMS = 20;
  private static final int MAX_HISTORY_PAGE_SIZE = 20;

  private final CustomerRepository customerRepository;
  private final PetRepository petRepository;
  private final AppointmentRepository appointmentRepository;
  private final MedicalRecordRepository medicalRecordRepository;

  @Override
  public List<AppointmentStatusResponse> getAppointmentStatuses(UUID petId, UUID currentUserId) {
    Pet ownedPet = requireOwnedPet(petId, currentUserId);

    Pageable limit = PageRequest.of(0, MAX_STATUS_ITEMS);

    return appointmentRepository
        .findOwnerPetAppointments(ownedPet.getId(), ownedPet.getCustomer().getId(), limit)
        .stream()
        .map(this::toAppointmentStatusResponse)
        .toList();
  }

  @Override
  public Page<VisitHistoryResponse> getCompletedHistory(
      UUID petId, UUID currentUserId, Pageable pageable) {
    Pet ownedPet = requireOwnedPet(petId, currentUserId);

    Pageable safePageable =
        PageRequest.of(
            Math.max(pageable.getPageNumber(), 0),
            Math.min(Math.max(pageable.getPageSize(), 1), MAX_HISTORY_PAGE_SIZE));

    return medicalRecordRepository
        .findOwnerPetHistory(
            ownedPet.getId(),
            ownedPet.getCustomer().getId(),
            MedicalRecordStatus.COMPLETED,
            safePageable)
        .map(this::toVisitHistoryResponse);
  }

  private Pet requireOwnedPet(UUID petId, UUID currentUserId) {
    Customer customer =
        customerRepository
            .findByUser_Id(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thú cưng"));

    /*
     * Pet không tồn tại và pet của người khác cùng trả 404,
     * tránh tiết lộ UUID hợp lệ.
     */
    return petRepository
        .findByIdAndCustomerIdAndDeletedAtIsNull(petId, customer.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thú cưng"));
  }

  private AppointmentStatusResponse toAppointmentStatusResponse(Appointment appointment) {
    return new AppointmentStatusResponse(
        appointment.getId(),
        appointment.getStatus().name(),
        appointment.getStartAt(),
        appointment.getService().getName());
  }

  private VisitHistoryResponse toVisitHistoryResponse(MedicalRecord medicalRecord) {
    Appointment appointment = medicalRecord.getAppointment();

    String doctorName =
        medicalRecord.getDoctor() == null
            ? "Chưa cập nhật"
            : medicalRecord.getDoctor().getFullName();

    String healthStatus =
        medicalRecord.getHealthStatus() == null ? null : medicalRecord.getHealthStatus().name();

    return new VisitHistoryResponse(
        medicalRecord.getId(),
        appointment.getId(),
        appointment.getStartAt(),
        medicalRecord.getUpdatedAt(),
        doctorName,
        appointment.getService().getName(),
        healthStatus,
        medicalRecord.getWeightKg(),
        medicalRecord.getDiagnosis(),
        medicalRecord.getTreatmentPlan());
  }
}
