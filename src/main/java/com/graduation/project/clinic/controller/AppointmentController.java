package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.dto.req.CreateAppointmentRequest;
import com.graduation.project.clinic.dto.req.UpdateAppointmentStatusRequest;
import com.graduation.project.clinic.dto.resp.AvailableSlotResponse;
import com.graduation.project.clinic.entity.AppointmentStatus;
import com.graduation.project.clinic.service.AppointmentService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/appointments")
@RequiredArgsConstructor
public class AppointmentController {

  private final AppointmentService appointmentService;

  // POST /api/clinic/appointments - Đặt lịch khám

  // GET /api/clinic/appointments/{id} - Chi tiết lịch khám

  // GET /api/clinic/appointments?customerId= - Lịch sử theo khách (paged)
  // PATCH /api/clinic/appointments/{id}/status - Đổi trạng thái lịch

  @GetMapping("/api/clinic/services/{id}/available-slots")
  public ApiResp<Object> getAvailableSlots(
      @PathVariable("id") UUID serviceId,
      @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    List<AvailableSlotResponse> slots = appointmentService.getAvailableSlots(serviceId, date);
    return ApiResp.builder().data(slots).build(); // dùng đúng factory ApiResp của bạn
  }

  @PostMapping
  public ResponseEntity<ApiResp<AppointmentDto>> create(
      @Valid @RequestBody CreateAppointmentRequest request) {
    AppointmentDto appointment = appointmentService.create(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResp.<AppointmentDto>builder()
            .message("Đặt lịch khám thành công")
            .data(appointment)
            .build());
  }

  // @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'DOCTOR')")
  @GetMapping("/management")
  @PreAuthorize("""
      hasAnyAuthority(
          'ROLE_ADMIN',
          'ROLE_MANAGER',
          'ROLE_RECEPTIONIST',
          'ROLE_DOCTOR'
      )
      """)
  public ResponseEntity<ApiResp<Page<AppointmentDto>>> getForManagement(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      @RequestParam(required = false) AppointmentStatus status,
      @PageableDefault(size = 100, sort = "startAt") Pageable pageable) {
    Page<AppointmentDto> appointments = appointmentService.getForManagement(startDate, endDate, status, pageable);

    return ResponseEntity.ok(
        ApiResp.<Page<AppointmentDto>>builder()
            .message("Lấy danh sách lịch khám thành công")
            .data(appointments)
            .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<AppointmentDto>> getById(
      @PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResp.<AppointmentDto>builder()
            .message("OK")
            .data(appointmentService.getById(id))
            .build());
  }

  @GetMapping
  public ResponseEntity<ApiResp<Page<AppointmentDto>>> getByCustomer(
      @RequestParam UUID customerId,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<AppointmentDto> appointments = appointmentService.getByCustomer(customerId, pageable);

    return ResponseEntity.ok(
        ApiResp.<Page<AppointmentDto>>builder()
            .message("OK")
            .data(appointments)
            .build());
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("""
      hasAnyAuthority(
          'ROLE_ADMIN',
          'ROLE_RECEPTIONIST'
      )
      """)
  public ResponseEntity<ApiResp<AppointmentDto>> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateAppointmentStatusRequest request) {
    AppointmentDto appointment = appointmentService.updateStatus(id, request);

    return ResponseEntity.ok(
        ApiResp.<AppointmentDto>builder()
            .message("Cập nhật trạng thái thành công")
            .data(appointment)
            .build());
  }

  @PatchMapping("/{id}/call-status")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_RECEPTIONIST')")
  public ResponseEntity<ApiResp<AppointmentDto>> updateCallStatus(
      @PathVariable UUID id,
      @RequestParam boolean isCalled) {
    AppointmentDto appointment = appointmentService.updateCallStatus(id, isCalled);

    return ResponseEntity.ok(
        ApiResp.<AppointmentDto>builder()
            .message("Cập nhật trạng thái gọi điện thành công")
            .data(appointment)
            .build());
  }
}
