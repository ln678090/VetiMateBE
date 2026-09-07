package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.customer.projection.CustomerAccountIdentityProjection;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetManagementServiceImpl implements PetManagementService {

  private final PetRepository petRepository;
  private final CustomerRepository customerRepository;

  @Override
  public Page<PetManagementSummary> search(
      String keyword,
      PetSpecies species,
      Boolean deleted,
      UUID customerId,
      Pageable pageable) {
    Page<Pet> petPage = petRepository.findAll(
        PetSpecification.managementFilter(
            keyword,
            species,
            deleted,
            customerId),
        pageable);

    Map<UUID, CustomerAccountIdentityProjection> identityByCustomerId = loadAccountIdentities(
        petPage.getContent()
            .stream()
            .map(Pet::getCustomer)
            .map(Customer::getId)
            .collect(Collectors.toSet()));

    return petPage.map(
        pet -> toSummary(
            pet,
            identityByCustomerId.get(
                pet.getCustomer().getId())));
  }

  @Override
  public PetManagementSummary getById(UUID petId) {
    Pet pet = petRepository
        .findById(petId)
        .orElseThrow(this::petNotFound);

    return toSummary(
        pet,
        loadAccountIdentity(
            pet.getCustomer().getId()));
  }

  @Override
  @Transactional
  public PetManagementSummary create(
      ManagementPetRequest request) {
    Customer customer = requireCustomer(request.customerId());

    Pet pet = new Pet();
    pet.setCustomer(customer);

    applyRequest(pet, request);

    Pet savedPet = petRepository.save(pet);

    return toSummary(
        savedPet,
        loadAccountIdentity(customer.getId()));
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

    Pet savedPet = petRepository.save(pet);

    return toSummary(
        savedPet,
        loadAccountIdentity(customer.getId()));
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

    return toSummary(
        pet,
        loadAccountIdentity(
            pet.getCustomer().getId()));
  }

  private Customer requireCustomer(UUID customerId) {
    return customerRepository
        .findById(customerId)
        .orElseThrow(
            () -> new ResourceNotFoundException(
                "Không tìm thấy chủ nuôi"));
  }

  private Pet requirePetForUpdate(UUID petId) {
    return petRepository
        .findByIdForUpdate(petId)
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

  private Map<UUID, CustomerAccountIdentityProjection> loadAccountIdentities(
      Collection<UUID> customerIds) {
    if (customerIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return customerRepository
        .findAccountIdentitiesByCustomerIds(
            customerIds)
        .stream()
        .collect(
            Collectors.toMap(
                CustomerAccountIdentityProjection::getCustomerId,
                Function.identity(),
                (first, ignored) -> first));
  }

  private CustomerAccountIdentityProjection loadAccountIdentity(UUID customerId) {
    return customerRepository
        .findAccountIdentitiesByCustomerIds(
            Collections.singleton(customerId))
        .stream()
        .findFirst()
        .orElse(null);
  }

  private PetManagementSummary toSummary(
      Pet pet,
      CustomerAccountIdentityProjection identity) {
    Customer customer = pet.getCustomer();

    String fullName = identity == null
        ? customer.getUser().getFullName()
        : identity.getFullName();

    String phone = identity == null
        ? customer.getUser().getPhone()
        : identity.getPhone();

    String email = identity == null
        ? customer.getUser().getEmail()
        : identity.getEmail();

    return new PetManagementSummary(
        pet.getId(),
        pet.getName(),
        pet.getSpecies(),
        pet.getBreed(),
        pet.getGender(),
        pet.getBirthDate(),
        pet.getWeightKg(),
        customer.getId(),
        fullName,
        phone,
        email,
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

    return normalized.isEmpty()
        ? null
        : normalized;
  }

  private ResourceNotFoundException petNotFound() {
    return new ResourceNotFoundException(
        "Không tìm thấy thú cưng");
  }

}
