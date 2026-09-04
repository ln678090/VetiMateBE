package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayClinicInvoiceRequest {
  @NotNull(message = "Payment method is required")
  private String paymentMethod; // CASH, CARD, BANK_TRANSFER, VNPAY, MOMO
}
