package com.graduation.project.staff.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.staff.dto.EligibleUserResponse;
import com.graduation.project.staff.dto.StaffResponse;
import com.graduation.project.staff.dto.req.CreateStaffRequest;
import com.graduation.project.staff.dto.req.DeactivateStaffRequest;
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
import org.springframework.security.core.Authentication;
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
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
public class StaffController {

  private final StaffService staffService;

  @GetMapping("/eligible-users")
  public ResponseEntity<ApiResp<Page<EligibleUserResponse>>> searchEligibleUsers(
      @RequestParam(required = false) String keyword,

      @PageableDefault(size = 20, sort = "fullName") Pageable pageable) {
    Page<EligibleUserResponse> users = staffService.searchEligibleUsers(
        keyword,
        pageable);

    return ResponseEntity.ok(
        ApiResp
            .<Page<EligibleUserResponse>>builder()
            .message(
                "Lấy danh sách tài khoản hợp lệ thành công")
            .data(users)
            .build());
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

    return ResponseEntity.ok(
        ApiResp
            .<Page<StaffResponse>>builder()
            .message(
                "Lấy danh sách nhân viên thành công")
            .data(staffPage)
            .build());
  }

  @GetMapping("/{staffId}")
  public ResponseEntity<ApiResp<StaffResponse>> getById(
      @PathVariable UUID staffId) {
    StaffResponse staff = staffService.getById(staffId);

    return ResponseEntity.ok(
        ApiResp.<StaffResponse>builder()
            .message(
                "Lấy thông tin nhân viên thành công")
            .data(staff)
            .build());
  }

  @PostMapping
  public ResponseEntity<ApiResp<StaffResponse>> create(
      @Valid @RequestBody CreateStaffRequest request,

      Authentication authentication) {
    UUID actorUserId = SecurityUtils.currentUserId(authentication);

    StaffResponse staff = staffService.create(
        request,
        actorUserId);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(
            ApiResp.<StaffResponse>builder()
                .message(
                    "Tiếp nhận nhân viên thành công")
                .data(staff)
                .build());
  }

  @PutMapping("/{staffId}")
  public ResponseEntity<ApiResp<StaffResponse>> update(
      @PathVariable UUID staffId,

      @Valid @RequestBody UpdateStaffRequest request,

      Authentication authentication) {
    UUID actorUserId = SecurityUtils.currentUserId(authentication);

    StaffResponse staff = staffService.update(
        staffId,
        request,
        actorUserId);

    return ResponseEntity.ok(
        ApiResp.<StaffResponse>builder()
            .message(
                "Cập nhật nhân viên thành công")
            .data(staff)
            .build());
  }

  @PostMapping("/{staffId}/deactivate")
  public ResponseEntity<ApiResp<StaffResponse>> deactivate(
      @PathVariable UUID staffId,

      @Valid @RequestBody DeactivateStaffRequest request,

      Authentication authentication) {
    UUID actorUserId = SecurityUtils.currentUserId(authentication);

    StaffResponse staff = staffService.deactivate(
        staffId,
        request.reason(),
        actorUserId);

    return ResponseEntity.ok(
        ApiResp.<StaffResponse>builder()
            .message(
                "Ngừng hoạt động nhân viên thành công")
            .data(staff)
            .build());
  }
}
