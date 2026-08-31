package com.graduation.project.clinic.dto.req;

import com.graduation.project.clinic.entity.QueueType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record QueueTicketRequest(
    @NotNull(message = "Queue type is required")
    QueueType queueType,
    
    UUID appointmentId
) {
}
