
package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.OwnerPetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OwnerPetService {

  Page<PetDto> getMyPets(
      UUID currentUserId,
      Pageable pageable);

  PetDto getMyPet(
      UUID petId,
      UUID currentUserId);

  PetDto createMyPet(
      OwnerPetRequest request,
      UUID currentUserId);

  PetDto updateMyPet(
      UUID petId,
      OwnerPetRequest request,
      UUID currentUserId);

  void deleteMyPet(
      UUID petId,
      UUID currentUserId);
}
