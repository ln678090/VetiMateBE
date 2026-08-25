package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.PetManagementSummary;
import com.graduation.project.clinic.dto.req.ManagementPetRequest;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.entity.PetSpecies;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.clinic.repository.specification.PetSpecification;
import com.graduation.project.clinic.service.PetManagementService;
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
public class PetManagementServiceImpl
    implements PetManagementService {

  private final PetRepository petRepository;
  private final CustomerRepository customerRepository;

  @Override
  public Page<PetManagementSummary> search(
      String keyword,
      PetSpecies species,
      Boolean deleted,
      UUID customerId,
      Pageable pageable) {
    return petRepository.findAll(
        PetSpecification.managementFilter(
            keyword,
            species,
            deleted,
            customerId),
        pageable).map(this::toSummary);
  }

  @Override
  public PetManagementSummary getById(UUID petId) {
    return petRepository.findById(petId)
        .map(this::toSummary)
        .orElseThrow(this::petNotFound);
  }

  @Override
  @Transactional
  public PetManagementSummary create(
      ManagementPetRequest request) {
    Customer customer = requireCustomer(request.customerId());

    Pet pet = new Pet();
    pet.setCustomer(customer);

    applyRequest(pet, request);

    return toSummary(petRepository.save(pet));
  }

  @Override
  @Transactional
  public PetManagementSummary update(
      UUID petId,
      ManagementPetRequest request) {
    Pet pet = requirePetForUpdate(petId);

    if (pet.getDeletedAt() != null) {
      throw petNotFound();
    }

    Customer customer = requireCustomer(request.customerId());

    pet.setCustomer(customer);
    applyRequest(pet, request);

    return toSummary(petRepository.save(pet));
  }

  @Override
  @Transactional
  public void softDelete(UUID petId) {
    Pet pet = requirePetForUpdate(petId);

    if (pet.getDeletedAt() == null) {
      pet.setDeletedAt(Instant.now());
      petRepository.save(pet);
    }
  }

  @Override
  @Transactional
  public PetManagementSummary restore(UUID petId) {
    Pet pet = requirePetForUpdate(petId);

    if (pet.getDeletedAt() != null) {
      pet.setDeletedAt(null);
      petRepository.save(pet);
    }

    return toSummary(pet);
  }

  private Customer requireCustomer(UUID customerId) {
    return customerRepository.findById(customerId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Không tìm thấy chủ nuôi"));
  }

  private Pet requirePetForUpdate(UUID petId) {
    return petRepository.findByIdForUpdate(petId)
        .orElseThrow(this::petNotFound);
  }

  private void applyRequest(
      Pet pet,
      ManagementPetRequest request) {
    pet.setName(request.name().trim());
    pet.setSpecies(request.species());
    pet.setBreed(normalize(request.breed()));
    pet.setGender(normalize(request.gender()));
    pet.setBirthDate(request.birthDate());
    pet.setWeightKg(request.weightKg());
  }

  private PetManagementSummary toSummary(Pet pet) {
    Customer customer = pet.getCustomer();

    return new PetManagementSummary(
        pet.getId(),
        pet.getName(),
        pet.getSpecies(),
        pet.getBreed(),
        pet.getGender(),
        pet.getBirthDate(),
        pet.getWeightKg(),

        customer.getId(),
        customer.getFullName(),
        customer.getPhone(),
        customer.getEmail(),

        pet.getCurrentHealthStatus(),
        pet.getCurrentHealthNote(),
        pet.getLastExaminedAt(),

        pet.getDeletedAt() != null,
        pet.getDeletedAt());
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
