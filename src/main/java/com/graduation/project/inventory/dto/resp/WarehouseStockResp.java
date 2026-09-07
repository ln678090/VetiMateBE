package com.graduation.project.inventory.dto.resp;

import java.math.BigDecimal;
import java.util.UUID;

public record WarehouseStockResp(
    UUID id, String name, String categoryName, String brandName, Integer stockQuantity) {
  public WarehouseStockResp(
      UUID id, String name, String categoryName, String brandName, BigDecimal sumQty) {
    this(id, name, categoryName, brandName, sumQty != null ? sumQty.intValue() : 0);
  }
}
