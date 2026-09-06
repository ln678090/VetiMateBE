package com.graduation.project.inventory.dto.resp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StockVoucherItemResp(
    UUID id,
    UUID medicineId,
    String medicineName,
    UUID productId,
    String productName,
    String batchCode,
    LocalDate expiryDate,
    BigDecimal quantity,
    BigDecimal unitPrice,
    String note) {}
