package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.AvailableSlotDto;
import com.graduation.project.clinic.dto.ClinicServiceDto;
import com.graduation.project.clinic.dto.req.ClinicServiceRequest;
import com.graduation.project.clinic.service.AppointmentService;
import com.graduation.project.clinic.service.ClinicServiceService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/services")
@RequiredArgsConstructor
public class ClinicServiceController {
  private final AppointmentService appointmentService;
  private final ClinicServiceService clinicServiceService;

  // GET /api/clinic/services/{id}/available-slots?date=2026-07-25
  @GetMapping("/{id}/available-slots")
  public ApiResp<List<AvailableSlotDto>> getAvailableSlots(
      @PathVariable UUID id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    List<AvailableSlotDto> slots = appointmentService.getAvailableSlots(id, date);
    return ApiResp.<List<AvailableSlotDto>>builder()
        .message("Lấy khung giờ trống thành công")
        .data(slots)
        .build();
  }

  // POST /api/clinic/services - Tạo dịch vụ
  @PostMapping
  public ResponseEntity<ApiResp<ClinicServiceDto>> create(@Valid @RequestBody ClinicServiceRequest request) {
    ClinicServiceDto dto = clinicServiceService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResp.<ClinicServiceDto>builder().message("Tạo dịch vụ thành công").data(dto).build());
  }

  // PUT /api/clinic/services/{id} - Sửa dịch vụ
  @PutMapping("/{id}")
  public ResponseEntity<ApiResp<ClinicServiceDto>> update(@PathVariable UUID id,
      @Valid @RequestBody ClinicServiceRequest request) {
    ClinicServiceDto dto = clinicServiceService.update(id, request);
    return ResponseEntity.ok(
        ApiResp.<ClinicServiceDto>builder().message("Cập nhật dịch vụ thành công").data(dto).build());
  }

  // GET /api/clinic/services/{id} - Chi tiết dịch vụ
  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<ClinicServiceDto>> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResp.<ClinicServiceDto>builder().message("OK").data(clinicServiceService.getById(id)).build());
  }

  // GET /api/clinic/services?activeOnly= - List dịch vụ (paged)
  @GetMapping
  public ResponseEntity<ApiResp<Page<ClinicServiceDto>>> list(
      @RequestParam(defaultValue = "false") boolean activeOnly,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<ClinicServiceDto> page = clinicServiceService.list(activeOnly, pageable);
    return ResponseEntity.ok(
        ApiResp.<Page<ClinicServiceDto>>builder().message("OK").data(page).build());
  }

  // DELETE /api/clinic/services/{id} - Xóa dịch vụ
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResp<Void>> delete(@PathVariable UUID id) {
    clinicServiceService.delete(id);
    return ResponseEntity.ok(
        ApiResp.<Void>builder().message("Xóa dịch vụ thành công").build());
  }
}
