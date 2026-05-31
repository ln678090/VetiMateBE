package com.graduation.project.auth.exception;

import com.graduation.project.common.resp.ApiResp;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.graduation.project")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthExceptionHandler {
  /** Xử lý lỗi đăng nhập sai tài khoản hoặc mật khẩu */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResp<Void>> handleBadCredentialsException(BadCredentialsException ex) {
    log.warn("Auth failed: {}", ex.getMessage());

    // Mặc định trả về lỗi đăng nhập
    String responseMessage = "Email hoặc mật khẩu không chính xác";

    // Nếu là lỗi từ hàm refreshToken
    if (ex.getMessage() != null && ex.getMessage().contains("Refresh Token")) {
      responseMessage = ex.getMessage();
    }

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            .message(responseMessage)
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiResp);
  }

  /** Xử lý lỗi tài khoản bị khóa (is_enabled = false) */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResp<Void>> handleDisabledException(DisabledException ex) {
    log.warn("Login failed: Account disabled");

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            // .code(String.valueOf(HttpStatus.FORBIDDEN.value())) // 403
            .message("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResp);
  }
}
