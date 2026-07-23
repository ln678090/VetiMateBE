package com.graduation.project.clinic.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.graduation.project.clinic.entity.PetSpecies;

public record PetDto(
    UUID id,
    UUID customerId,
    String customerName, // flatten: ten chu pet
    String name,
    PetSpecies species,
    String breed,
    String gender,
    LocalDate birthDate,
    BigDecimal weightKg,
    String note,
    Instant createdAt,
    Instant updatedAt) {
}
