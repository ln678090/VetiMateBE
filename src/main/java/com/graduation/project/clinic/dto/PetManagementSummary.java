package com.graduation.project.clinic.dto;

import com.graduation.project.clinic.entity.PetSpecies;
import com.graduation.project.clinic.enums.PetHealthStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PetManagementSummary(
    UUID id,
    String name,
    PetSpecies species,
    String breed,
    String gender,
    LocalDate birthDate,
    BigDecimal weightKg,
    UUID customerId,
    String customerName,
    String customerPhone,
    String customerEmail,
    PetHealthStatus currentHealthStatus,
    String currentHealthNote,
    Instant lastExaminedAt,
    boolean deleted,
    Instant deletedAt) {}
