package com.graduation.project.common.exception;

import com.graduation.project.common.resp.ApiResp;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // uu tien thap nhat , run nen ko co thang nao bat
public class GlobalExceptionHandler {
  /** Xử lý lỗi Validation (từ @Valid trong Controller) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResp<Map<String, String>>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    log.warn("Validation error occurred: {}", ex.getMessage());

    Map<String, String> errors = new HashMap<>();

    // Lấy tất cả các lỗi từ các field bị sai
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    ApiResp<Map<String, String>> apiResp =
        ApiResp.<Map<String, String>>builder()
            //                .code(String.valueOf(HttpStatus.BAD_REQUEST.value())) // 400
            .message("Dữ liệu đầu vào không hợp lệ")
            .data(errors) // Trả về danh sách lỗi cho FE dễ tô đỏ ô input
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResp);
  }

  /** Xử lý lỗi IllegalArgumentException (Ví dụ: Email đã tồn tại) */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResp<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("Illegal Argument: {}", ex.getMessage());

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            //                .code(String.valueOf(HttpStatus.BAD_REQUEST.value())) // 400
            .message(ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResp);
  }

  /** Xử lý lỗi UsernameNotFoundException */
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiResp<Void>> usernameNotFoundException(UsernameNotFoundException ex) {
    log.error("Username not found exception: {}", ex.getMessage());

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            //                .code(String.valueOf(HttpStatus.BAD_REQUEST.value())) // 400
            .message(ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResp);
  }

  /** Xử lý tất cả các RuntimeException và Exception không xác định khác (Lỗi sập server) */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResp<Void>> handleGlobalException(Exception ex) {
    // Lỗi này là lỗi nghiêm trọng (NPE, Database sập...), cần in StackTrace để debug
    log.error("Internal Server Error: ", ex);

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            //                .code(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())) // 500
            .message("Hệ thống đang bận hoặc xảy ra lỗi nghiêm trọng. Vui lòng thử lại sau!")
            // Tuyệt đối không trả 'ex.getMessage()' cho FE ở lỗi 500 để bảo mật (tránh lộ SQL)
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResp);
  }

  //  Xử lý khi không tìm thấy tài nguyên (Post/Comment)
  // @ExceptionHandler(ResourceNotFoundException.class)
  // public ResponseEntity<ApiResp<Void>> handleResourceNotFoundException(ResourceNotFoundException
  // ex) {
  //     ApiResp<Void> apiResp = ApiResp.<Void>builder()
  //             .message(ex.getMessage())
  //             .timestamp(Instant.now().toString())
  //             .build();
  //     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResp);
  // }

  //  Xử lý khi user cố tình xóa bài/bình luận của người khác
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResp<Void>> handleAccessDeniedException(AccessDeniedException ex) {
    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            .message(ex.getMessage())
            .timestamp(Instant.now().toString())
            .build();
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResp);
  }
}
