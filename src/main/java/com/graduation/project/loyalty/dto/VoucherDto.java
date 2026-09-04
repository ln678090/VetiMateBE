package com.graduation.project.loyalty.dto;

import com.graduation.project.loyalty.entity.DiscountType;
import com.graduation.project.loyalty.entity.CustomerTier;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class VoucherDto {
    private UUID id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscount;
    private Integer pointsRequired;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private CustomerTier requiredTier;
    private LocalDateTime createdAt;
}
