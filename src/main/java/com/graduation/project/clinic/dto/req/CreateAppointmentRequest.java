package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentRequest(

    @NotNull(message = "petId bat buoc") UUID petId,

    @NotNull(message = "serviceId bat buoc") UUID serviceId,

    @NotNull(message = "Thoi gian bat dau bat buoc") @Future(message = "Thoi gian dat lich phai o tuong lai") Instant startAt,

    @Size(max = 500) String note) {
}
