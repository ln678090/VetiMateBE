package com.graduation.project.clinic.examination.controller;

import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.AppointmentStatusResponse;
import com.graduation.project.clinic.examination.dto.OwnerPetHistoryDtos.VisitHistoryResponse;
import com.graduation.project.clinic.examination.service.StaffPetHistoryService;
import com.graduation.project.common.resp.ApiResp;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/staff/customers/{customerId}/pets/{petId}")
@RequiredArgsConstructor
@PreAuthorize(
    "hasAnyAuthority("
        + "'ROLE_RECEPTIONIST',"
        + "'ROLE_ADMIN',"
        + "'ROLE_MANAGER',"
        + "'ROLE_DOCTOR'"
        + ")")
public class StaffPetHistoryController {

  private final StaffPetHistoryService staffPetHistoryService;

  @GetMapping("/appointment-statuses")
  public ApiResp<List<AppointmentStatusResponse>> getAppointmentStatuses(
      @PathVariable UUID customerId, @PathVariable UUID petId) {
    List<AppointmentStatusResponse> statuses =
        staffPetHistoryService.getAppointmentStatuses(customerId, petId);

    return ApiResp.<List<AppointmentStatusResponse>>builder()
        .message("Lấy trạng thái lịch khám thành công")
        .data(statuses)
        .build();
  }

  @GetMapping("/history")
  public ApiResp<Page<VisitHistoryResponse>> getCompletedHistory(
      @PathVariable UUID customerId, @PathVariable UUID petId, Pageable pageable) {
    Page<VisitHistoryResponse> history =
        staffPetHistoryService.getCompletedHistory(customerId, petId, pageable);

    return ApiResp.<Page<VisitHistoryResponse>>builder()
        .message("Lấy lịch sử khám thành công")
        .data(history)
        .build();
  }
}
