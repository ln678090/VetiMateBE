package com.graduation.project.inventory.dto.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record VoucherItemRequest(
    UUID medicineId,
    UUID productId,
    UUID batchId,
    @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0.01", message = "Số lượng phải > 0")
        BigDecimal quantity,
    @DecimalMin(value = "0", message = "Đơn giá >= 0") BigDecimal unitPrice,
    String note,
    // For IMPORT: batch fields
    String batchCode,
    String expiryDate,
    UUID supplierId) {}
