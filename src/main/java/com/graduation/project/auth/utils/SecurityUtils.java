package com.graduation.project.auth.utils;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class SecurityUtils {

  private SecurityUtils() {}

  public static UUID currentUserId(Authentication authentication) {
    if (authentication instanceof JwtAuthenticationToken jwtAuth) {
      return UUID.fromString(jwtAuth.getName());
    }
    throw new IllegalStateException("Không lấy được userId từ JWT");
  }
}
