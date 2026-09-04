package com.graduation.project.clinic.dto.resp;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
  private UUID id;
  private UUID productId;
  private String productName;
  private String productImage;
  private BigDecimal price;
  private Integer quantity;
}
