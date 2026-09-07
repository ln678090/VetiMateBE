package com.graduation.project;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("ci")
class SchemaValidationTest {

  // ❌ XÓA hoặc COMMENT dòng Mock RsaKeyProperties cũ đi
  // @MockBean private RsaKeyProperties rsaKeyProperties;

  // 🟢 BỔ SUNG 1: Mock trực tiếp Bean JWKSource để Spring Boot không chạy vào
  // logic của JWKConfig nữa
  @MockBean private JWKSource<SecurityContext> jwkSource;

  // 🟢 BỔ SUNG 2: Mock luôn bộ mã hóa JwtEncoder để TokenService không bị lỗi
  // tiêm phụ thuộc
  @MockBean private JwtEncoder jwtEncoder;

  // Giữ nguyên các Mock Bean Redis cũ
  @MockBean private StringRedisTemplate stringRedisTemplate;
  @MockBean private RedisTemplate<String, String> redisTemplate;

  @Test
  void contextLoads() {
    // Nếu Spring context khởi động thành công nghĩa là:
    // - Flyway đã migrate DB đúng
    // - Hibernate validate schema thành công
    // => Entity và DB schema khớp nhau
  }
}
