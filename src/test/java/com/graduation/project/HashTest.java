package com.graduation.project;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class HashTest {
    @Test
    public void generateHash() {
        Argon2PasswordEncoder encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        System.out.println("HASH_IS_HERE: " + encoder.encode("Staff@123"));
    }
}
