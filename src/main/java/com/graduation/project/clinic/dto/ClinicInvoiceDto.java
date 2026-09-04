package com.graduation.project.clinic.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicInvoiceDto {
  private UUID id;
  private String invoiceCode;
  private String customerName;
  private String customerPhone;
  private String petName;
  private String type; // CLINIC, SHOP, MIXED
  private String status; // DRAFT, PENDING, PAID, CANCELLED
  private BigDecimal subtotal;
  private BigDecimal discountAmount;
  private BigDecimal totalAmount;
  private String paymentMethod;
  private Instant paidAt;
  private String note;
  private Instant createdAt;

  private List<ClinicInvoiceItemDto> items;
}
