package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OwnerPetHistoryService {

  List<AppointmentStatusResponse> getAppointmentStatuses(UUID petId, UUID currentUserId);

  Page<VisitHistoryResponse> getCompletedHistory(UUID petId, UUID currentUserId, Pageable pageable);
}
