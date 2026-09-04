package com.graduation.project.clinic.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClinicInvoiceItemDto {
  private UUID id;
  private String name;
  private BigDecimal quantity;
  private BigDecimal unitPrice;
  private BigDecimal total;
  private String type; // SERVICE, PRODUCT, MEDICINE
}
