
package com.graduation.project.clinic.examination.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class OwnerPetHistoryDtos {

  private OwnerPetHistoryDtos() {
  }

  public record AppointmentStatusResponse(
      UUID appointmentId,
      String status,
      Instant startAt,
      String serviceName) {
  }

  public record VisitHistoryResponse(
      UUID medicalRecordId,
      UUID appointmentId,
      Instant examinedAt,
      Instant completedAt,
      String doctorName,
      String serviceName,
      String healthStatus,
      BigDecimal weightKg,
      String diagnosis,
      String treatmentPlan) {
  }
}
