package com.graduation.project.clinic.dto;

import java.time.Instant;
import java.util.UUID;

public record CustomerDto(
    UUID id,
    UUID userId,
    String fullName,
    String phone,
    String email,
    String address,
    String note,
    Instant createdAt,
    Instant updatedAt) {}
