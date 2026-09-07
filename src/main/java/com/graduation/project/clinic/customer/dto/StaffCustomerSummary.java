package com.graduation.project.clinic.customer.dto;

import java.time.Instant;
import java.util.UUID;

public record StaffCustomerSummary(
    UUID id,
    String fullName,
    String phone,
    String email,
    long petCount,
    String latestAppointmentStatus,
    Instant latestAppointmentAt) {}
