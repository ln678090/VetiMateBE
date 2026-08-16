package com.graduation.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ProjectApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProjectApplication.class, args);
  }

  @org.springframework.context.annotation.Bean
  org.springframework.boot.CommandLineRunner initStaffPassword(
      com.graduation.project.user.repository.UserRepository userRepo,
      org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
    return args -> {
      userRepo.findByEmail("shopstaff@vetimate.com").ifPresent(user -> {
        user.setPassword(passwordEncoder.encode("123456"));
        userRepo.save(user);
      });
    };
  }
}
