package com.graduation.project.inventory.service;

import com.graduation.project.inventory.dto.req.CreateImportVoucherReq;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.entity.Supplier;
import java.util.List;
import java.util.UUID;

public interface InventoryService {
  StockVoucherResp createImportVoucher(CreateImportVoucherReq req, UUID createdByUserId);

  StockVoucherResp approveVoucher(UUID voucherId, UUID approvedByUserId);

  List<StockVoucherResp> getAllVouchers();

  List<Supplier> getAllSuppliers();
}
