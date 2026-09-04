package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.PetRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PetService {
  PetDto create(PetRequest request);

  PetDto update(UUID id, PetRequest request);

  PetDto getById(UUID id);

  Page<PetDto> getByCustomer(UUID customerId, Pageable pageable);

  void softDelete(UUID petId, UUID customerId);

  void delete(UUID id);
}
