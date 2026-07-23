package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.ClinicServiceDto;
import com.graduation.project.clinic.dto.req.ClinicServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClinicServiceService {
  ClinicServiceDto create(ClinicServiceRequest request);

  ClinicServiceDto update(UUID id, ClinicServiceRequest request);

  ClinicServiceDto getById(UUID id);

  Page<ClinicServiceDto> list(boolean activeOnly, Pageable pageable);

  void delete(UUID id);
}
