package com.graduation.project.user.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.user.dto.req.AdminChangePasswordReq;
import com.graduation.project.user.dto.req.AdminCreateUserReq;
import com.graduation.project.user.dto.resp.UserAdminResp;
import com.graduation.project.user.service.UserService;
import com.graduation.project.auth.utils.SecurityUtils;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<ApiResp<Page<UserAdminResp>>> getAllUsers(Pageable pageable) {
    Page<UserAdminResp> users = userService.getAllUsers(pageable);
    return ResponseEntity.ok(
        ApiResp.<Page<UserAdminResp>>builder()
            .message("Thành công")
            .data(users)
            .timestamp(Instant.now().toString())
            .build());
  }

  @PutMapping("/{userId}/password")
  public ResponseEntity<ApiResp<String>> adminChangePassword(
      @PathVariable UUID userId,
      @Valid @RequestBody AdminChangePasswordReq request) {
    
    userService.adminChangePassword(userId, request.newPassword());
    
    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Đổi mật khẩu tài khoản thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build());
  }

  @PutMapping("/{userId}/toggle-status")
  public ResponseEntity<ApiResp<String>> toggleUserStatus(@PathVariable UUID userId) {
    userService.toggleUserStatus(userId);
    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Cập nhật trạng thái tài khoản thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build());
  }

  @PostMapping
  public ResponseEntity<ApiResp<UserAdminResp>> createUser(
      @Valid @RequestBody AdminCreateUserReq request,
      org.springframework.security.core.Authentication authentication) {
    UUID actorUserId = SecurityUtils.currentUserId(authentication);
    UserAdminResp newUser = userService.adminCreateUser(request, actorUserId);
    return ResponseEntity.ok(
        ApiResp.<UserAdminResp>builder()
            .message("Tạo tài khoản mới thành công")
            .data(newUser)
            .timestamp(Instant.now().toString())
            .build());
  }
}
