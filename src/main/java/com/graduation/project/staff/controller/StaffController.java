
package com.graduation.project.staff.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.staff.dto.StaffResponse;
import com.graduation.project.staff.dto.req.CreateStaffRequest;
import com.graduation.project.staff.dto.req.UpdateStaffRequest;
import com.graduation.project.staff.entity.StaffRoleType;
import com.graduation.project.staff.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class StaffController {

  private final StaffService staffService;

  @PostMapping
  public ResponseEntity<ApiResp<StaffResponse>> create(
      @Valid @RequestBody CreateStaffRequest request) {
    StaffResponse staff = staffService.create(request);

    ApiResp<StaffResponse> response = ApiResp.<StaffResponse>builder()
        .message("Tạo nhân viên thành công")
        .data(staff)
        .build();

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(response);
  }

  @GetMapping
  public ResponseEntity<ApiResp<Page<StaffResponse>>> search(
      @RequestParam(required = false) String keyword,

      @RequestParam(required = false) StaffRoleType roleType,

      @RequestParam(required = false) Boolean active,

      @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
    Page<StaffResponse> staffPage = staffService.search(
        keyword,
        roleType,
        active,
        pageable);

    ApiResp<Page<StaffResponse>> response = ApiResp.<Page<StaffResponse>>builder()
        .message(
            "Lấy danh sách nhân viên thành công")
        .data(staffPage)
        .build();

    return ResponseEntity.ok(response);
  }

  @GetMapping("/{staffId}")
  public ResponseEntity<ApiResp<StaffResponse>> getById(
      @PathVariable UUID staffId) {
    StaffResponse staff = staffService.getById(staffId);

    ApiResp<StaffResponse> response = ApiResp.<StaffResponse>builder()
        .message(
            "Lấy thông tin nhân viên thành công")
        .data(staff)
        .build();

    return ResponseEntity.ok(response);
  }

  @PutMapping("/{staffId}")
  public ResponseEntity<ApiResp<StaffResponse>> update(
      @PathVariable UUID staffId,
      @Valid @RequestBody UpdateStaffRequest request) {
    StaffResponse staff = staffService.update(staffId, request);

    ApiResp<StaffResponse> response = ApiResp.<StaffResponse>builder()
        .message("Cập nhật nhân viên thành công")
        .data(staff)
        .build();

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{staffId}")
  public ResponseEntity<ApiResp<Void>> deactivate(
      @PathVariable UUID staffId) {
    staffService.deactivate(staffId);

    ApiResp<Void> response = ApiResp.<Void>builder()
        .message(
            "Ngừng hoạt động nhân viên thành công")
        .data(null)
        .build();

    return ResponseEntity.ok(response);
  }
}
