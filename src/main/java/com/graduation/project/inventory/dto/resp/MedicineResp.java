package com.graduation.project.inventory.dto.resp;

import java.math.BigDecimal;
import java.util.UUID;

public record MedicineResp(
    UUID id,
    String name,
    String sku,
    String unit,
    BigDecimal minStock,
    BigDecimal importPrice,
    BigDecimal sellPrice,
    Boolean isActive,
    BigDecimal totalStock) {}
