package com.graduation.project.inventory.dto.req;

import com.graduation.project.inventory.entity.VoucherType;
import com.graduation.project.inventory.entity.WarehouseLocation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateVoucherRequest(
    @NotNull(message = "Loại phiếu không được để trống") VoucherType type,
    WarehouseLocation sourceWarehouse,
    WarehouseLocation destinationWarehouse,
    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự") String note,
    @NotEmpty(message = "Phiếu phải có ít nhất 1 dòng") @Valid List<VoucherItemRequest> items) {}
