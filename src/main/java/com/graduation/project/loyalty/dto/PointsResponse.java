package com.graduation.project.loyalty.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import com.graduation.project.loyalty.entity.CustomerTier;

@Data
@Builder
public class PointsResponse {
    private Integer totalPoints;
    private Integer availablePoints;
    private BigDecimal totalSpending;
    private CustomerTier tier;
}
