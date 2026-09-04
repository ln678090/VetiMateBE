package com.graduation.project.staff.dto.req;

import com.graduation.project.staff.entity.StaffRoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateStaffRequest(
    @NotNull UUID userId,
    @NotNull StaffRoleType roleType,
    @NotBlank @Size(min = 10, max = 500) String reason) {}
