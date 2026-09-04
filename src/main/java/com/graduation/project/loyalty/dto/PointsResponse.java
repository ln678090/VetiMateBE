package com.graduation.project.loyalty.dto;

import com.graduation.project.loyalty.entity.CustomerTier;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointsResponse {
  private Integer totalPoints;
  private Integer availablePoints;
  private BigDecimal totalSpending;
  private CustomerTier tier;
}
