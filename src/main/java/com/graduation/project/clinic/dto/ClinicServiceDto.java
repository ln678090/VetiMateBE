package com.graduation.project.clinic.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ClinicServiceDto(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    Integer durationMin,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt) {
}
