package com.graduation.project.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.project.common.resp.ApiResp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// custom AuthenticationEntryPoint cho 401
public class CustomAuthenticationEntryPointOauth2 implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException {

    ApiResp<Void> apiResp =
        ApiResp.<Void>builder()
            .message("Bearer token không hợp lệ hoặc không được cung cấp")
            .build();

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getWriter(), apiResp);
  }
}
