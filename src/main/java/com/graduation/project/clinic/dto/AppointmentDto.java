package com.graduation.project.clinic.dto;

import com.graduation.project.clinic.entity.AppointmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AppointmentDto(
    UUID id,
    UUID customerId,
    String customerName, // flatten
    UUID petId,
    String petName, // flatten
    UUID serviceId,
    String serviceName, // flatten
    Instant startAt,
    Instant endAt,
    Integer durationMin,
    BigDecimal priceSnapshot,
    AppointmentStatus status,
    String note,
    Boolean isCalledToConfirm,
    Instant createdAt,
    Instant updatedAt) {}
