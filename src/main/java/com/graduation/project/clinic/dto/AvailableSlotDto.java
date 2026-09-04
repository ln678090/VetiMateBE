package com.graduation.project.clinic.dto;

import java.time.Instant;

/** Một khung giờ có thể đặt. available=false nếu đã có người đặt. */
public record AvailableSlotDto(Instant startAt, Instant endAt, boolean available) {}
