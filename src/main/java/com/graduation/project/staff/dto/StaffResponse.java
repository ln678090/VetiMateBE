package com.graduation.project.staff.dto;

import com.graduation.project.staff.entity.StaffRoleType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StaffResponse(
    UUID id,
    UUID userId,
    String fullName,
    String phone,
    StaffRoleType roleType,
    String licenseNumber,
    BigDecimal baseSalary,
    BigDecimal commissionRate,
    boolean active,
    Instant createdAt) {
}
