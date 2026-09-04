package com.graduation.project.inventory.mapper;

import com.graduation.project.inventory.dto.resp.StockBatchResp;
import com.graduation.project.inventory.dto.resp.StockVoucherItemResp;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.dto.resp.SupplierResp;
import com.graduation.project.inventory.entity.StockBatch;
import com.graduation.project.inventory.entity.StockVoucher;
import com.graduation.project.inventory.entity.StockVoucherItem;
import com.graduation.project.inventory.entity.Supplier;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

  // ===== Supplier =====
  SupplierResp toSupplierResp(Supplier supplier);

  List<SupplierResp> toSupplierRespList(List<Supplier> suppliers);

  // ===== StockBatch =====
  @Mapping(target = "medicineId", source = "medicine.id")
  @Mapping(target = "medicineName", source = "medicine.name")
  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "supplierId", source = "supplier.id")
  @Mapping(target = "supplierName", source = "supplier.name")
  @Mapping(target = "isExpired", expression = "java(batch.isExpired())")
  @Mapping(target = "isNearExpiry", expression = "java(batch.isNearExpiry(30))")
  StockBatchResp toBatchResp(StockBatch batch);

  List<StockBatchResp> toBatchRespList(List<StockBatch> batches);

  // ===== StockVoucherItem =====
  @Mapping(target = "medicineId", source = "medicine.id")
  @Mapping(target = "medicineName", source = "medicine.name")
  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(target = "batchCode", source = "batch.batchCode")
  StockVoucherItemResp toVoucherItemResp(StockVoucherItem item);

  List<StockVoucherItemResp> toVoucherItemRespList(List<StockVoucherItem> items);

  // ===== StockVoucher =====
  @Mapping(target = "items", source = "items")
  @Mapping(
      target = "itemCount",
      expression = "java(voucher.getItems() != null ? voucher.getItems().size() : 0)")
  StockVoucherResp toVoucherResp(StockVoucher voucher);

  List<StockVoucherResp> toVoucherRespList(List<StockVoucher> vouchers);
}
