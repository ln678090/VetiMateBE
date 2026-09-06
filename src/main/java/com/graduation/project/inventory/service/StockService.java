package com.graduation.project.inventory.service;

import com.graduation.project.inventory.dto.req.CreateVoucherRequest;
import com.graduation.project.inventory.dto.resp.InventoryDashboardResp;
import com.graduation.project.inventory.dto.resp.StockBatchResp;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface StockService {

  /** Tạo phiếu kho (DRAFT) */
  StockVoucherResp createVoucher(CreateVoucherRequest request);

  /** Duyệt phiếu → cập nhật tồn kho */
  StockVoucherResp approveVoucher(UUID voucherId, UUID approvedBy);

  /** Hủy phiếu */
  StockVoucherResp cancelVoucher(UUID voucherId);

  /** Lấy phiếu theo ID */
  StockVoucherResp getVoucherById(UUID id);

  /** Danh sách phiếu kho — có filter type/status, phân trang */
  Page<StockVoucherResp> getVouchers(VoucherType type, VoucherStatus status, int page, int size);

  /** Lô hàng theo medicine (FEFO) */
  List<StockBatchResp> getBatchesByMedicine(UUID medicineId);

  /** Lô hàng theo product (FEFO) */
  List<StockBatchResp> getBatchesByProduct(UUID productId);

  /** Cảnh báo: lô cận date (30 ngày) theo kho */
  List<StockBatchResp> getNearExpiryBatches(
      com.graduation.project.inventory.entity.WarehouseLocation warehouse);

  default List<StockBatchResp> getNearExpiryBatches() {
    return getNearExpiryBatches(com.graduation.project.inventory.entity.WarehouseLocation.STORAGE);
  }

  /** Cảnh báo: lô đã hết hạn theo kho */
  List<StockBatchResp> getExpiredBatches(
      com.graduation.project.inventory.entity.WarehouseLocation warehouse);

  default List<StockBatchResp> getExpiredBatches() {
    return getExpiredBatches(com.graduation.project.inventory.entity.WarehouseLocation.STORAGE);
  }

  /** Danh sách lô hàng theo kho */
  List<StockBatchResp> getBatchesByWarehouse(
      com.graduation.project.inventory.entity.WarehouseLocation warehouse);

  /** Xuất nhanh 1 lô hết hạn từ Kho bảo quản lên Kho bác sĩ */
  StockVoucherResp exportExpiredBatchToDoctor(UUID batchId, UUID currentUserId);

  /** Xuất nhanh toàn bộ các lô hết hạn từ Kho bảo quản lên Kho bác sĩ */
  StockVoucherResp exportAllExpiredBatchesToDoctor(UUID currentUserId);

  /** Dashboard tổng quan kho */
  InventoryDashboardResp getDashboard();
}
