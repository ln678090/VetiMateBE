
package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.OwnerPetRequest;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.mapper.PetMapper;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.clinic.service.OwnerPetService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerPetServiceImpl implements OwnerPetService {

  private final CustomerRepository customerRepository;
  private final PetRepository petRepository;
  private final PetMapper petMapper;

  @Override
  public Page<PetDto> getMyPets(
      UUID currentUserId,
      Pageable pageable) {
    Customer customer = requireOwnerCustomer(currentUserId);

    return petRepository
        .findByCustomerIdAndDeletedAtIsNull(
            customer.getId(),
            pageable)
        .map(petMapper::toDto);
  }

  @Override
  public PetDto getMyPet(
      UUID petId,
      UUID currentUserId) {
    Customer customer = requireOwnerCustomer(currentUserId);

    Pet pet = petRepository
        .findByIdAndCustomerIdAndDeletedAtIsNull(
            petId,
            customer.getId())
        .orElseThrow(this::petNotFound);

    return petMapper.toDto(pet);
  }

  @Override
  @Transactional
  public PetDto createMyPet(
      OwnerPetRequest request,
      UUID currentUserId) {
    Customer customer = requireOwnerCustomer(currentUserId);

    Pet pet = new Pet();
    pet.setCustomer(customer);

    applyRequest(pet, request);

    return petMapper.toDto(petRepository.save(pet));
  }

  @Override
  @Transactional
  public PetDto updateMyPet(
      UUID petId,
      OwnerPetRequest request,
      UUID currentUserId) {
    Customer customer = requireOwnerCustomer(currentUserId);

    Pet pet = requireOwnedPetForUpdate(
        petId,
        customer.getId());

    applyRequest(pet, request);

    return petMapper.toDto(petRepository.save(pet));
  }

  @Override
  @Transactional
  public void deleteMyPet(
      UUID petId,
      UUID currentUserId) {
    Customer customer = requireOwnerCustomer(currentUserId);

    Pet pet = requireOwnedPetForUpdate(
        petId,
        customer.getId());

    pet.setDeletedAt(Instant.now());
    petRepository.save(pet);
  }

  private Customer requireOwnerCustomer(UUID currentUserId) {
    return customerRepository
        .findByUserId(currentUserId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Không tìm thấy hồ sơ khách hàng"));
  }

  private Pet requireOwnedPetForUpdate(
      UUID petId,
      UUID customerId) {
    Pet pet = petRepository
        .findByIdForUpdate(petId)
        .orElseThrow(this::petNotFound);

    if (pet.getDeletedAt() != null
        || !pet.getCustomer().getId().equals(customerId)) {
      throw petNotFound();
    }

    return pet;
  }

  private void applyRequest(
      Pet pet,
      OwnerPetRequest request) {
    pet.setName(request.name().trim());
    pet.setSpecies(request.species());
    pet.setBreed(normalize(request.breed()));
    pet.setGender(normalize(request.gender()));
    pet.setBirthDate(request.birthDate());
    pet.setWeightKg(request.weightKg());
    // pet.setColor(normalize(request.color()));
    // pet.setNotes(normalize(request.notes()));

  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();
    return normalized.isEmpty() ? null : normalized;
  }

  private ResourceNotFoundException petNotFound() {
    return new ResourceNotFoundException(
        "Không tìm thấy thú cưng");
  }
}
