package com.graduation.project.user.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.user.dto.UserProfileResp;
import com.graduation.project.user.dto.UpdateProfileRequest;
import com.graduation.project.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<ApiResp<UserProfileResp>> getMyProfile(Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    UserProfileResp profile = userService.getMyProfile(userId);
    return ResponseEntity.ok(
        ApiResp.<UserProfileResp>builder()
            .message("Lấy thông tin cá nhân thành công")
            .data(profile)
            .build());
  }

  @PutMapping("/me/profile")
  public ResponseEntity<ApiResp<String>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request,
      Authentication authentication) {
    UUID userId = UUID.fromString(authentication.getName());
    userService.updateProfile(userId, request);
    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Cập nhật thông tin cá nhân thành công")
            .data("Success")
            .build());
  }
}
