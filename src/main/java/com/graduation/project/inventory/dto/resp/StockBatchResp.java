package com.graduation.project.inventory.dto.resp;

import com.graduation.project.inventory.entity.WarehouseLocation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record StockBatchResp(
    UUID id,
    String batchCode,
    WarehouseLocation warehouse,
    BigDecimal quantity,
    BigDecimal remainingQty,
    BigDecimal importPrice,
    LocalDate expiryDate,
    OffsetDateTime receivedAt,
    // medicine info
    UUID medicineId,
    String medicineName,
    // product info
    UUID productId,
    String productName,
    // supplier info
    UUID supplierId,
    String supplierName,
    // computed
    Boolean isExpired,
    Boolean isNearExpiry) {}
