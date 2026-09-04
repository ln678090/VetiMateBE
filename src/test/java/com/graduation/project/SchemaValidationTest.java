package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Integration test để validate rằng Flyway migration và Spring Entity khớp nhau. */
@SpringBootTest
@ActiveProfiles("ci")
class SchemaValidationTest {

  @MockBean private RedisTemplate<String, String> redisTemplate;

  @Test
  void contextLoads() {
    // Nếu Spring context khởi động thành công nghĩa là:
    // - Flyway đã migrate DB đúng
    // - Hibernate validate schema thành công
    // => Entity và DB schema khớp nhau
  }
}
