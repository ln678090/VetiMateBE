package com.graduation.project.clinic.examination.service;

import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OwnerPetHistoryService {

  List<AppointmentStatusResponse> getAppointmentStatuses(
      UUID petId,
      UUID currentUserId);

  Page<VisitHistoryResponse> getCompletedHistory(
      UUID petId,
      UUID currentUserId,
      Pageable pageable);
}
