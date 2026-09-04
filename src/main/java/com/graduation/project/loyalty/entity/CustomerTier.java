package com.graduation.project.loyalty.entity;

import java.math.BigDecimal;

public enum CustomerTier {
  MEMBER(BigDecimal.ZERO),
  BRONZE(new BigDecimal("3000000")),
  SILVER(new BigDecimal("7000000")),
  GOLD(new BigDecimal("10000000")),
  DIAMOND(new BigDecimal("15000000"));

  private final BigDecimal requiredSpending;

  CustomerTier(BigDecimal requiredSpending) {
    this.requiredSpending = requiredSpending;
  }

  public BigDecimal getRequiredSpending() {
    return requiredSpending;
  }

  public static CustomerTier calculateTier(BigDecimal totalSpending) {
    if (totalSpending == null) {
      return MEMBER;
    }
    if (totalSpending.compareTo(DIAMOND.getRequiredSpending()) >= 0) return DIAMOND;
    if (totalSpending.compareTo(GOLD.getRequiredSpending()) >= 0) return GOLD;
    if (totalSpending.compareTo(SILVER.getRequiredSpending()) >= 0) return SILVER;
    if (totalSpending.compareTo(BRONZE.getRequiredSpending()) >= 0) return BRONZE;
    return MEMBER;
  }
}
