package com.graduation.project.auth.service;

import com.graduation.project.auth.dto.privateDto.TokenPair;
import com.graduation.project.auth.dto.req.LoginRequest;
import com.graduation.project.auth.dto.req.RegisterRequest;
import com.graduation.project.auth.dto.req.ChangePasswordRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

public interface AuthService {
  TokenPair login(LoginRequest request);

  @Transactional
  TokenPair register(RegisterRequest request);

  @Transactional
  TokenPair refreshToken(String oldRefreshToken);

  // Trong file AuthService.java
  // TokenPair loginWithGoogle(String idTokenString);
  void logout(String refreshToken);

  @Transactional
  void changePassword(UUID userId, ChangePasswordRequest request);
}
