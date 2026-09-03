package com.graduation.project;

import com.graduation.project.auth.dto.req.LoginRequest;
import com.graduation.project.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoginTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testLogin() {
        try {
            System.out.println("TESTING LOGIN...");
            var tokenPair = authService.login(new LoginRequest("demaciagarenss@gmail.com", "123456"));
            System.out.println("LOGIN SUCCESS: " + tokenPair.accessToken());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
