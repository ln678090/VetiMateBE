package com.graduation.project.clinic.dto.req;

import com.graduation.project.clinic.entity.QueueStatus;
import jakarta.validation.constraints.NotNull;

public record QueueStatusUpdateRequest(
    @NotNull(message = "Queue status is required") QueueStatus status) {}
