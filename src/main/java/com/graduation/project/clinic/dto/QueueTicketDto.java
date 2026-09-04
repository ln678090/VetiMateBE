package com.graduation.project.clinic.dto;

import com.graduation.project.clinic.entity.QueueStatus;
import com.graduation.project.clinic.entity.QueueType;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record QueueTicketDto(
    UUID id,
    UUID appointmentId,
    String customerName,
    String petName,
    String serviceName,
    LocalDate queueDate,
    QueueType queueType,
    Integer ticketNumber,
    QueueStatus status,
    Instant calledAt,
    Instant completedAt,
    Instant createdAt) {}
