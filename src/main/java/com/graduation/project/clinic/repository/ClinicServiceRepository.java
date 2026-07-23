package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.ClinicService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClinicServiceRepository extends JpaRepository<ClinicService, UUID> {

  Page<ClinicService> findByIsActiveTrue(Pageable pageable);
}
