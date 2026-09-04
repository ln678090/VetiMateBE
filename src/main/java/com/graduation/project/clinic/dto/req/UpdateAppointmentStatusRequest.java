package com.graduation.project.clinic.dto.req;

import com.graduation.project.clinic.entity.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
    @NotNull(message = "status bat buoc") AppointmentStatus status) {}
