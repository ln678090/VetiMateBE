package com.graduation.project.auth.config.jwt;

import com.graduation.project.auth.keys.RefreshTokenPrefix;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
  private final JwtEncoder jwtEncoder;

  private final RedisTemplate<String, String> redisTemplate;
  private final RefreshTokenPrefix REFRESH_TOKEN_KEY_PREFIX;

  public String generateAccessToken(UUID userId, String roles) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("connecthub-api")
        .issuedAt(now)
        .expiresAt(now.plus(15, ChronoUnit.MINUTES)) // Hết hạn sau 15 phút
        .subject(userId.toString())
        .claim("roles", roles)
        .build();
    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateInternalToken(UUID userId) {
    Instant now = Instant.now();

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("connecthub-internal")
        .issuedAt(now)
        .expiresAt(now.plus(5, ChronoUnit.MINUTES)) // ngắn thôi cho an toàn
        .subject(userId.toString())
        .claim("scope", "internal")
        .build();

    return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public String generateAccessTokenFromAuth(Authentication authentication, UUID userId) {
    String roles = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));
    return generateAccessToken(userId, roles);
  }

  public String generateRefreshToken(UUID userId) {
    String refreshToken = UUID.randomUUID().toString();
    String redisKey = REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken;
    redisTemplate.opsForValue().set(
        redisKey,
        userId.toString(),
        30,
        TimeUnit.DAYS);
    return refreshToken;
  }

  // public String generateRefreshToken(UUID userId, String roles) {
  // String refreshToken = UUID.randomUUID().toString();
  // // Cấu trúc lưu: "ID|ROLES" (Ví dụ: "123e4567-e89b-12d3...|ROLE_USER")
  // String redisValue =
  // userId.toString()
  // + "|"
  // + roles
  // + "key"
  // + REFRESH_TOKEN_KEY_PREFIX.key()
  // + ":"
  // + refreshToken;
  //
  // redisTemplate
  // .opsForValue()
  // .set(
  // REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken,
  // redisValue,
  // 30,
  // TimeUnit.DAYS // Kéo dài 30 ngày (1 tháng mới bắt login lại)
  // );
  // return refreshToken;
  // }
  //
  public boolean validateRefreshToken(String refreshToken) {
    Boolean a = redisTemplate.hasKey(REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken);
    if (a != null)
      return a;
    return false;
  }

  public UUID getUserIdFromRefreshToken(String refreshToken) {
    String redisKey = REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken;

    String userId = redisTemplate.opsForValue().get(redisKey);

    if (userId == null) {
      throw new IllegalArgumentException(
          "Refresh token không hợp lệ hoặc đã hết hạn");
    }

    return UUID.fromString(userId);
  }
  // public UUID getUserIdFromRefreshToken(String refreshToken) {
  // String userIdStr =
  // redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX.key() + ":" +
  // refreshToken);
  // if (userIdStr == null) {
  // throw new IllegalArgumentException("Refresh Token không hợp lệ hoặc đã hết
  // hạn");
  // }
  // return UUID.fromString(userIdStr);
  // }

  public String getRedisValueFromRefreshToken(String refreshToken) {
    return redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken);
  }

  public Long getRefreshTokenTTL(String refreshToken) {
    return redisTemplate.getExpire(
        REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken, TimeUnit.DAYS);
  }

  public void deleteRefreshToken(String refreshToken) {
    redisTemplate.delete(REFRESH_TOKEN_KEY_PREFIX.key() + ":" + refreshToken);
  }
}
