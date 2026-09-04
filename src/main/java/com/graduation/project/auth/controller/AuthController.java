package com.graduation.project.auth.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.auth.dto.privateDto.TokenPair;
import com.graduation.project.auth.dto.req.ChangePasswordRequest;
import com.graduation.project.auth.dto.req.LoginRequest;
import com.graduation.project.auth.dto.req.RegisterRequest;
import com.graduation.project.auth.dto.resp.AuthResponse;
import com.graduation.project.auth.service.AuthService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final String REFRESH_TOKEN_REQUEST_BODY = "refreshToken";
  private final AuthService authService;
  private static final int REFRESH_TOKEN_MAX_AGE = 2592000; //  ngày

  //    @PostMapping("/loginv1")
  //    public ResponseEntity<?> post(@RequestBody LoginRequest value) {
  //        TokenPair tokens = authService.login(value);
  //        return  ResponseEntity.ok(tokens);
  //    }

  @PostMapping("/login")
  public ResponseEntity<ApiResp<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    TokenPair tokens = authService.login(request);
    return buildAuthResponse(tokens, HttpStatus.OK, "Đăng nhập thành công", false);
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResp<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    TokenPair tokens = authService.register(request);
    return buildAuthResponse(tokens, HttpStatus.CREATED, "Đăng ký thành công", false);
  }

  @PostMapping("/login-mobile")
  public ResponseEntity<ApiResp<AuthResponse>> loginMobile(
      @Valid @RequestBody LoginRequest request) {
    TokenPair tokens = authService.login(request);
    return buildAuthResponse(tokens, HttpStatus.OK, "Đăng nhập thành công", true);
  }

  @PostMapping("/register-mobile")
  public ResponseEntity<ApiResp<AuthResponse>> registerMobile(
      @Valid @RequestBody RegisterRequest request) {
    TokenPair tokens = authService.register(request);
    return buildAuthResponse(tokens, HttpStatus.CREATED, "Đăng ký thành công", true);
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResp<AuthResponse>> refresh(
      @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken) {
    if (cookieRefreshToken == null || cookieRefreshToken.isEmpty()) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Không tìm thấy Refresh Token trong Cookie");
    }
    TokenPair newTokens = authService.refreshToken(cookieRefreshToken);
    return buildAuthResponse(newTokens, HttpStatus.OK, "Làm mới Token thành công", false);
  }

  @PostMapping("/refresh-mobile")
  public ResponseEntity<ApiResp<AuthResponse>> refreshMobile(
      @RequestBody(required = false) Map<String, String> body) {
    String refreshToken =
        body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY)
            ? body.get(REFRESH_TOKEN_REQUEST_BODY)
            : null;
    if (refreshToken == null || refreshToken.isEmpty()) {
      throw new org.springframework.security.authentication.BadCredentialsException(
          "Không tìm thấy Refresh Token trong body");
    }
    TokenPair newTokens = authService.refreshToken(refreshToken);
    return buildAuthResponse(newTokens, HttpStatus.OK, "Làm mới Token thành công", true);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResp<String>> logout(
      @CookieValue(name = "refresh_token", required = false) String cookieRefreshToken,
      @RequestBody(required = false) Map<String, String> body) {
    String refreshToken =
        body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY)
            ? body.get(REFRESH_TOKEN_REQUEST_BODY)
            : cookieRefreshToken;
    authService.logout(refreshToken);
    ResponseCookie deleteCookie;
    if (!(body != null && body.containsKey(REFRESH_TOKEN_REQUEST_BODY))) {
      deleteCookie =
          ResponseCookie.from("refresh_token", "")
              .httpOnly(true)
              .secure(false)
              .path("/api/auth")
              .maxAge(0)
              .sameSite("None")
              .build();
    } else {
      deleteCookie = null;
    }

    ApiResp<String> response =
        ApiResp.<String>builder()
            .message("Đăng xuất thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie != null ? deleteCookie.toString() : "")
        .body(response);
  }

  @GetMapping("/test")
  public ResponseEntity<ApiResp<String>> test() {

    //        String threadName = Thread.currentThread().getName();

    // Log ra console để bạn quan sát trực tiếp trên Render Log
    //        System.out.println("===> Request đang được xử lý bởi Thread: " + threadName);

    return ResponseEntity.ok(
        ApiResp.<String>builder()
            //                .timestamp(Instant.now().toString())
            //                .data("Current Thread: " + threadName)
            .build());
  }

  @PutMapping("/change-password")
  public ResponseEntity<ApiResp<String>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
    Object principal = authentication.getPrincipal();
    UUID userId;

    if (principal instanceof Jwt jwt) {
      userId = UUID.fromString(jwt.getSubject());
    } else if (principal instanceof CustomUserDetails userDetails) {
      userId = userDetails.id();
    } else {
      throw new IllegalStateException("Authentication principal không hợp lệ");
    }

    authService.changePassword(userId, request);

    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Đổi mật khẩu thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build());
  }

  // @PostMapping("/login/google")
  // public ResponseEntity<ApiResp<AuthResponse>> loginWithGoogle(@Valid @RequestBody
  // GoogleLoginRequest request) {
  //     try {
  //         // isMobile = false (Gắn Cookie giống bản Login web thông thường)
  //         // Nếu bạn làm Mobile thì có thể truyền cờ tùy chỉnh hoặc làm thêm API riêng
  //         TokenPair tokens = authService.loginWithGoogle(request.idToken());
  //         return buildAuthResponse(tokens, HttpStatus.OK, "Đăng nhập Google thành công", false);

  //     } catch (DisabledException ex) {
  //         return ResponseEntity.status(HttpStatus.FORBIDDEN)
  //                 .body(ApiResp.<AuthResponse>builder()
  //                         .message("Tài khoản của bạn bị khóa. Vui lòng liên hệ Admin.")
  //                         .timestamp(Instant.now().toString())
  //                         .build());
  //     } catch (Exception ex) {
  //         return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
  //                 .body(ApiResp.<AuthResponse>builder()
  //                         .message("Xác thực Google thất bại.")
  //                         .timestamp(Instant.now().toString())
  //                         .build());
  //     }
  // }
  /** Hàm tiện ích DUY NHẤT để gom chung logic tạo Cookie và bọc ApiResp */
  private ResponseEntity<ApiResp<AuthResponse>> buildAuthResponse(
      TokenPair tokens, HttpStatus status, String message, boolean isMobile) {

    ResponseCookie refreshCookie =
        ResponseCookie.from("refresh_token", tokens.refreshToken())
            .httpOnly(true)
            .secure(true) // production nên để true khi dùng HTTPS
            .path("/api/auth")
            .maxAge(REFRESH_TOKEN_MAX_AGE)
            .sameSite("None") // Strict, Lax
            .build();

    AuthResponse authResponse =
        new AuthResponse(tokens.accessToken(), isMobile ? tokens.refreshToken() : null, "Bearer");

    ApiResp<AuthResponse> apiResp =
        ApiResp.<AuthResponse>builder()
            .message(message)
            .data(authResponse)
            .timestamp(Instant.now().toString())
            .build();

    ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(status);

    if (!isMobile) {
      responseBuilder.header(HttpHeaders.SET_COOKIE, refreshCookie.toString());
      //            responseBuilder.header(HttpHeaders.SET_COOKIE,
      // String.format("refresh_token=%s",refreshCookie.toString()));
    }

    return responseBuilder.body(apiResp);
  }
}
