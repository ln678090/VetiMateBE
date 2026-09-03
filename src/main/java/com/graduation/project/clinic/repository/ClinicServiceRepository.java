package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.ClinicService;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicServiceRepository extends JpaRepository<ClinicService, UUID> {

  Optional<ClinicService> findByIdAndIsActiveTrue(UUID id);

  Page<ClinicService> findByIsActiveTrue(Pageable pageable);
}
