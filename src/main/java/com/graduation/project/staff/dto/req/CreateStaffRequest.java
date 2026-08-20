package com.graduation.project.staff.dto.req;

import com.graduation.project.staff.entity.StaffRoleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStaffRequest(@NotNull UUID userId, @NotNull StaffRoleType roleType,
    @NotBlank @Size(min = 10, max = 500) String reason) {
}
