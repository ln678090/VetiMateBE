package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ClinicServiceRequest(

    @NotBlank(message = "Ten dich vu khong duoc de trong") @Size(max = 150) String name,

    @Size(max = 500) String description,

    @NotNull(message = "Gia bat buoc") @DecimalMin(value = "0.0", message = "Gia khong the am") @Digits(integer = 10, fraction = 2) BigDecimal price,

    @NotNull(message = "Thoi luong bat buoc") @Min(value = 1, message = "Thoi luong phai > 0 phut") @Max(value = 1440, message = "Thoi luong toi da 1 ngay") Integer durationMin,

    Boolean isActive) {
}
