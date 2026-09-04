package com.graduation.project.clinic.dto.req;

import com.graduation.project.clinic.entity.PetSpecies;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PetRequest(
    @NotNull(message = "customerId bat buoc") UUID customerId,
    @NotBlank(message = "Ten pet khong duoc de trong") @Size(max = 100) String name,
    @NotNull(message = "Loài thú cưng không được để trống (chỉ nhận DOG hoặc CAT)")
        PetSpecies species,
    @Size(max = 100) String breed,
    @Pattern(regexp = "MALE|FEMALE|UNKNOWN", message = "gender phai la MALE, FEMALE hoac UNKNOWN")
        String gender,
    @PastOrPresent(message = "Ngay sinh khong the o tuong lai") LocalDate birthDate,
    @DecimalMin(value = "0.0", inclusive = false, message = "Can nang phai > 0")
        @Digits(integer = 4, fraction = 2)
        BigDecimal weightKg,
    @Size(max = 500) String note) {}
