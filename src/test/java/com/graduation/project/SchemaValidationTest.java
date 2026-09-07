package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("ci")
class SchemaValidationTest {
  @MockitoBean private RedisTemplate<String, String> redisTemplate;

  @Test
  void contextLoads() {
    // Nếu Spring context khởi động thành công nghĩa là:
    // - Flyway đã migrate DB đúng
    // - Hibernate validate schema thành công
    // => Entity và DB schema khớp nhau
  }
}
