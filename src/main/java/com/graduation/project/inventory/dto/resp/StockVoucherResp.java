package com.graduation.project.inventory.dto.resp;

import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StockVoucherResp(
    UUID id,
    VoucherType type,
    VoucherStatus status,
    UUID createdBy,
    UUID approvedBy,
    OffsetDateTime approvedAt,
    String note,
    List<StockVoucherItemResp> items,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    int itemCount) {}
