package com.graduation.project.auth.repository;

import com.graduation.project.auth.entity.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

  Optional<Role> findByName(String string);
}
