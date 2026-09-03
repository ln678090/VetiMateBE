package com.graduation.project.clinic.examination.dto;

import com.graduation.project.clinic.examination.entity.ServiceIndicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class ServiceIndicationDtos {
  private ServiceIndicationDtos() {
  }

  public record CreateRequest(
      @NotNull(message = "Dịch vụ không được để trống") UUID serviceId) {
  }

  public record CompleteRequest(
      @NotBlank(message = "Kết quả không được để trống") @Size(max = 5000, message = "Kết quả không được vượt quá 5000 ký tự") String resultNote) {
  }

  public record Response(
      UUID id,
      UUID medicalRecordId,
      UUID serviceId,
      String serviceName,
      ServiceIndicationStatus status,
      String resultNote,
      Instant createdAt) {
  }
}
