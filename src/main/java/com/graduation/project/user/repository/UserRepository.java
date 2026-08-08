package com.graduation.project.user.repository;

import com.graduation.project.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);
boolean existsByPhone(String phone);
  Optional<User> findByEmail(String email);
}
