package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class GenHashTest {
  @Test
  public void generate() {
    Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    System.out.println("HASH_IS_HERE: " + encoder.encode("123456"));
  }
}
