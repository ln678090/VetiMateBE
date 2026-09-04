package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.PetManagementSummary;
import com.graduation.project.clinic.dto.req.ManagementPetRequest;
import com.graduation.project.clinic.entity.PetSpecies;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PetManagementService {

  Page<PetManagementSummary> search(
      String keyword, PetSpecies species, Boolean deleted, UUID customerId, Pageable pageable);

  PetManagementSummary getById(UUID petId);

  PetManagementSummary create(ManagementPetRequest request);

  PetManagementSummary update(UUID petId, ManagementPetRequest request);

  void softDelete(UUID petId);

  PetManagementSummary restore(UUID petId);
}
