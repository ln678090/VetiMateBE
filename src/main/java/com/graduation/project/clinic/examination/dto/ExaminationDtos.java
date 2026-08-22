package com.graduation.project.clinic.examination.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.graduation.project.clinic.enums.PetHealthStatus;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;

public final class ExaminationDtos {

  private ExaminationDtos() {
  }

  public record SaveExaminationRequest(
      @Size(max = 5000) String symptoms,

      @Size(max = 5000) String diagnosis,

      @Size(max = 5000) String treatmentPlan,

      @DecimalMin(value = "0.01") @Digits(integer = 4, fraction = 2) BigDecimal weightKg,

      @NotNull PetHealthStatus healthStatus,

      @Size(max = 5000) String doctorNote) {
  }

  public record PrescriptionItemRequest(
      @NotNull(message = "Thuốc không được để trống") UUID medicineId,

      @NotNull(message = "Số lượng không được để trống") @DecimalMin(value = "0.01") BigDecimal quantity,

      @NotBlank(message = "Liều dùng không được để trống") @Size(max = 200) String dosage,

      @NotNull(message = "Số ngày không được để trống") @Positive Integer durationDays,

      @Size(max = 500) String note) {
  }

  public record ReplacePrescriptionsRequest(
      @NotNull(message = "Danh sách đơn thuốc không được null") List<@Valid PrescriptionItemRequest> items) {
  }

  public record PrescriptionResponse(
      UUID id,
      UUID medicineId,
      String medicineName,
      String unit,
      BigDecimal quantity,
      String dosage,
      Integer durationDays,
      String note) {
  }

  public record PrescriptionItemResponse(
      UUID id,
      UUID medicineId,
      String medicineName,
      String sku,
      String unit,
      BigDecimal quantity,
      String dosage,
      Integer durationDays,
      String note) {
  }

  public record MedicalRecordResponse(UUID id, UUID appointmentId, UUID petId, UUID doctorId, String symptoms,
      String diagnosis, String treatmentPlan, BigDecimal weightKg, PetHealthStatus healthStatus, String doctorNote,
      MedicalRecordStatus status, Instant createdAt, Instant updatedAt, List<PrescriptionItemResponse> prescriptions) {
  } // public record MedicalRecordResponse(
  // UUID id,
  // UUID appointmentId,
  // UUID petId,
  // UUID doctorId,
  // String symptoms,
  // String diagnosis,
  // String treatmentPlan,
  // BigDecimal weightKg,
  // String doctorNote,
  // String status,
  // Instant createdAt,
  // Instant updatedAt,
  // List<PrescriptionResponse> prescriptions) {
  // }

  public record MedicineOptionResponse(
      UUID id,
      String name,
      String sku,
      String unit,
      BigDecimal sellPrice) {
  }

  public record ExaminationHistoryResponse(
      UUID id,
      UUID appointmentId,
      UUID petId,
      String petName,
      String diagnosis,
      PetHealthStatus healthStatus,
      BigDecimal weightKg,
      Instant completedAt) {
  }

}
