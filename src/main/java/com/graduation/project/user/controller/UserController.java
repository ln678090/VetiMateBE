package com.graduation.project.user.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.user.dto.req.UpdateProfileRequest;
import com.graduation.project.user.dto.resp.UserProfileResp;
import com.graduation.project.user.service.UserService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResp<UserProfileResp>> getMyProfile(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        UUID userId;
        
        if (principal instanceof Jwt jwt) {
            userId = UUID.fromString(jwt.getSubject());
        } else if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.id();
        } else {
            throw new IllegalStateException("Authentication principal không hợp lệ");
        }

        UserProfileResp resp = userService.getMyProfile(userId);
        
        return ResponseEntity.ok(
                ApiResp.<UserProfileResp>builder()
                        .message("Thành công")
                        .data(resp)
                        .timestamp(Instant.now().toString())
                        .build());
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ApiResp<String>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        
        Object principal = authentication.getPrincipal();
        UUID userId;
        
        if (principal instanceof Jwt jwt) {
            userId = UUID.fromString(jwt.getSubject());
        } else if (principal instanceof CustomUserDetails userDetails) {
            userId = userDetails.id();
        } else {
            throw new IllegalStateException("Authentication principal không hợp lệ");
        }

        userService.updateProfile(userId, request);

        return ResponseEntity.ok(
                ApiResp.<String>builder()
                        .message("Cập nhật hồ sơ thành công")
                        .data("OK")
                        .timestamp(Instant.now().toString())
                        .build());
    }

}
