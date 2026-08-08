package com.graduation.project.clinic.dto.resp;

import java.time.LocalTime;

public record AvailableSlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        boolean available
) {}
