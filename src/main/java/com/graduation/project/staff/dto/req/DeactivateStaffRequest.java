package com.graduation.project.staff.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeactivateStaffRequest(
    @NotBlank(message = "Lý do không được để trống")
        @Size(min = 10, max = 500, message = "Lý do phải từ 10 đến 500 ký tự")
        String reason) {}
