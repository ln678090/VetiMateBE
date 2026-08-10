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
  StockVoucherResp approveVoucher(UUID voucherId);

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

  /** Cảnh báo: lô cận date (30 ngày) */
  List<StockBatchResp> getNearExpiryBatches();

  /** Cảnh báo: lô đã hết hạn */
  List<StockBatchResp> getExpiredBatches();

  /** Dashboard tổng quan kho */
  InventoryDashboardResp getDashboard();
}
