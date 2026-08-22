
package com.graduation.project.clinic.dto.req;

import com.graduation.project.clinic.entity.PetSpecies;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OwnerPetRequest(
    @NotBlank @Size(max = 100) String name,

    @NotNull PetSpecies species,

    @Size(max = 100) String breed,

    @Size(max = 20) String gender,

    @PastOrPresent LocalDate birthDate,

    @DecimalMin("0.01") BigDecimal weightKg,

    @Size(max = 100) String color,

    @Size(max = 1000) String notes) {
}
