package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StaffPetHistoryService {

  List<AppointmentStatusResponse> getAppointmentStatuses(UUID customerId, UUID petId);

  Page<VisitHistoryResponse> getCompletedHistory(UUID customerId, UUID petId, Pageable pageable);
}
