package com.graduation.project.loyalty.dto;

import com.graduation.project.loyalty.entity.CustomerTier;
import com.graduation.project.loyalty.entity.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateVoucherReq {
  @NotBlank private String code;

  private String description;

  @NotNull private DiscountType discountType;

  @NotNull
  @Min(0)
  private BigDecimal discountValue;

  @Min(0)
  private BigDecimal minOrderAmount;

  @Min(0)
  private BigDecimal maxDiscount;

  @NotNull
  @Min(0)
  private Integer pointsRequired;

  @Min(1)
  private Integer usageLimit;

  private LocalDateTime startDate;
  private LocalDateTime endDate;

  private Boolean isActive = true;

  private CustomerTier requiredTier;
}
