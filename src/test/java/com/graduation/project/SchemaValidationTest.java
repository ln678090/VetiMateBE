package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Integration test để validate rằng Flyway migration và Spring Entity khớp nhau. */
@SpringBootTest
@ActiveProfiles("ci")
class SchemaValidationTest {

  @Test
  void contextLoads() {
    // Nếu Spring context khởi động thành công nghĩa là:
    // - Flyway đã migrate DB đúng
    // - Hibernate validate schema thành công
    // => Entity và DB schema khớp nhau
  }
}
