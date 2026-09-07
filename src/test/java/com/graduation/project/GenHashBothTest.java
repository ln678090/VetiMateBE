package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class GenHashBothTest {
  @Test
  public void generateBothPasswords() {
    Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    System.out.println("HASH_123456: " + encoder.encode("123456"));
    System.out.println("HASH_123456789: " + encoder.encode("123456789"));
  }
}
