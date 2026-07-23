package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.PetRequest;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.mapper.PetMapper;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.clinic.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

  private final PetRepository petRepository;
  private final CustomerRepository customerRepository;
  private final PetMapper petMapper;

  @Override
  @Transactional
  public PetDto create(PetRequest request) {
    Customer customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng: " + request.customerId()));
    Pet pet = Pet.builder()
        .customer(customer)
        .name(request.name())
        .species(request.species())
        .breed(request.breed())
        .gender(request.gender())
        .birthDate(request.birthDate())
        .weightKg(request.weightKg())
        .note(request.note())
        .build();
    return petMapper.toDto(petRepository.save(pet));
  }

  @Override
  @Transactional
  public PetDto update(UUID id, PetRequest request) {
    Pet pet = petRepository.findByIdWithCustomer(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pet: " + id));
    // Cho đổi chủ nếu customerId khác
    if (request.customerId() != null && !request.customerId().equals(pet.getCustomer().getId())) {
      Customer newOwner = customerRepository.findById(request.customerId())
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng: " + request.customerId()));
      pet.setCustomer(newOwner);
    }
    pet.setName(request.name());
    pet.setSpecies(request.species());
    pet.setBreed(request.breed());
    pet.setGender(request.gender());
    pet.setBirthDate(request.birthDate());
    pet.setWeightKg(request.weightKg());
    pet.setNote(request.note());
    return petMapper.toDto(petRepository.save(pet));
  }

  @Override
  @Transactional(readOnly = true)
  public PetDto getById(UUID id) {
    Pet pet = petRepository.findByIdWithCustomer(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pet: " + id));
    return petMapper.toDto(pet);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PetDto> getByCustomer(UUID customerId, Pageable pageable) {
    return petRepository.findByCustomerId(customerId, pageable).map(petMapper::toDto);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!petRepository.existsById(id)) {
      throw new IllegalArgumentException("Không tìm thấy pet: " + id);
    }
    petRepository.deleteById(id);
  }
}
