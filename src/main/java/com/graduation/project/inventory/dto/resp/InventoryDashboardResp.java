package com.graduation.project.inventory.dto.resp;

import java.math.BigDecimal;

public record InventoryDashboardResp(
    long totalMedicines,
    long totalSuppliers,
    long lowStockCount,
    long nearExpiryCount,
    long expiredCount,
    long pendingVouchers,
    BigDecimal totalStockValue) {}
