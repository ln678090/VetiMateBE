package com.graduation.project.clinic.examination.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import com.graduation.project.clinic.examination.service.OwnerPetHistoryService;
import com.graduation.project.common.resp.ApiResp;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/me/pets/{petId}")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OwnerPetHistoryController {

  private final OwnerPetHistoryService ownerPetHistoryService;

  @GetMapping("/appointment-statuses")
  public ApiResp<List<AppointmentStatusResponse>> getAppointmentStatuses(
      @PathVariable UUID petId, Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    List<AppointmentStatusResponse> statuses =
        ownerPetHistoryService.getAppointmentStatuses(petId, currentUserId);

    return ApiResp.<List<AppointmentStatusResponse>>builder()
        .message("Lấy trạng thái lịch khám thành công")
        .data(statuses)
        .build();
  }

  @GetMapping("/history")
  public ApiResp<Page<VisitHistoryResponse>> getCompletedHistory(
      @PathVariable UUID petId, Pageable pageable, Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    Page<VisitHistoryResponse> history =
        ownerPetHistoryService.getCompletedHistory(petId, currentUserId, pageable);

    return ApiResp.<Page<VisitHistoryResponse>>builder()
        .message("Lấy lịch sử khám thành công")
        .data(history)
        .build();
  }
}
