package com.graduation.project.inventory.dto.resp;

import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import com.graduation.project.inventory.entity.WarehouseLocation;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StockVoucherResp(
    UUID id,
    VoucherType type,
    VoucherStatus status,
    WarehouseLocation sourceWarehouse,
    WarehouseLocation destinationWarehouse,
    UUID createdBy,
    UUID approvedBy,
    OffsetDateTime approvedAt,
    String note,
    List<StockVoucherItemResp> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    int itemCount) {}
