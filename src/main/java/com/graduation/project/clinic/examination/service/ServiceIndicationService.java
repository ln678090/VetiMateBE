package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.entity.ClinicService;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.CompleteRequest;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.CreateRequest;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.Response;
import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;
import com.graduation.project.clinic.examination.entity.ServiceIndication;
import com.graduation.project.clinic.examination.entity.ServiceIndicationStatus;
import com.graduation.project.clinic.examination.repository.MedicalRecordRepository;
import com.graduation.project.clinic.examination.repository.ServiceIndicationRepository;
import com.graduation.project.clinic.repository.ClinicServiceRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ServiceIndicationService {

  private final ServiceIndicationRepository indicationRepository;
  private final MedicalRecordRepository medicalRecordRepository;
  private final ClinicServiceRepository clinicServiceRepository;

  @Transactional(readOnly = true)
  public List<Response> getAll(UUID medicalRecordId, UUID currentUserId) {
    getOwnedRecord(medicalRecordId, currentUserId);

    return indicationRepository
        .findAllByMedicalRecordIdOrderByCreatedAtAsc(medicalRecordId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public Response create(UUID medicalRecordId, CreateRequest request, UUID currentUserId) {

    MedicalRecord medicalRecord = getOwnedRecord(medicalRecordId, currentUserId);

    requireEditableRecord(medicalRecord);

    ClinicService clinicService =
        clinicServiceRepository
            .findByIdAndIsActiveTrue(request.serviceId())
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Dịch vụ không tồn tại hoặc đã ngừng hoạt động"));

    boolean duplicated =
        indicationRepository.existsByMedicalRecordIdAndServiceIdAndStatus(
            medicalRecordId, request.serviceId(), ServiceIndicationStatus.PENDING);

    if (duplicated) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Dịch vụ này đang có chỉ định chờ xử lý");
    }

    ServiceIndication indication = new ServiceIndication();
    indication.setMedicalRecord(medicalRecord);
    indication.setService(clinicService);
    indication.setStatus(ServiceIndicationStatus.PENDING);

    return toResponse(indicationRepository.save(indication));
  }

  @Transactional
  public Response complete(UUID indicationId, CompleteRequest request, UUID currentUserId) {

    ServiceIndication indication = getOwnedPendingIndication(indicationId, currentUserId);

    requireEditableRecord(indication.getMedicalRecord());

    String resultNote = request.resultNote().trim();

    if (resultNote.isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Kết quả chỉ định không được để trống");
    }

    indication.setResultNote(resultNote);
    indication.setStatus(ServiceIndicationStatus.DONE);

    return toResponse(indication);
  }

  @Transactional
  public Response cancel(UUID indicationId, UUID currentUserId) {

    ServiceIndication indication = getOwnedPendingIndication(indicationId, currentUserId);

    requireEditableRecord(indication.getMedicalRecord());

    indication.setStatus(ServiceIndicationStatus.CANCELLED);
    indication.setResultNote(null);

    return toResponse(indication);
  }

  private MedicalRecord getOwnedRecord(UUID medicalRecordId, UUID currentUserId) {

    return medicalRecordRepository
        .findOwnedById(medicalRecordId, currentUserId)
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy phiếu khám"));
  }

  private ServiceIndication getOwnedPendingIndication(UUID indicationId, UUID currentUserId) {

    ServiceIndication indication =
        indicationRepository
            .findOwnedByIdForUpdate(indicationId, currentUserId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy chỉ định"));

    if (indication.getStatus() != ServiceIndicationStatus.PENDING) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Chỉ có thể cập nhật chỉ định đang chờ xử lý");
    }

    return indication;
  }

  private void requireEditableRecord(MedicalRecord medicalRecord) {
    if (medicalRecord.getStatus() != MedicalRecordStatus.IN_PROGRESS) {

      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Chỉ được cập nhật chỉ định khi phiếu khám đang thực hiện");
    }
  }

  private Response toResponse(ServiceIndication indication) {
    ClinicService service = indication.getService();

    return new Response(
        indication.getId(),
        indication.getMedicalRecord().getId(),
        service.getId(),
        service.getName(),
        indication.getStatus(),
        indication.getResultNote(),
        indication.getCreatedAt());
  }
}
