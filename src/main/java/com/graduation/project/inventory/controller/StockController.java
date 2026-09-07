package com.graduation.project.inventory.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.inventory.dto.req.CreateVoucherRequest;
import com.graduation.project.inventory.dto.resp.InventoryDashboardResp;
import com.graduation.project.inventory.dto.resp.StockBatchResp;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import com.graduation.project.inventory.entity.WarehouseLocation;
import com.graduation.project.inventory.service.StockService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAREHOUSE', 'ROLE_DOCTOR')")
public class StockController {

  private final StockService stockService;

  // ===== Dashboard =====

  @GetMapping("/dashboard")
  public ApiResp<InventoryDashboardResp> getDashboard() {
    return ApiResp.<InventoryDashboardResp>builder()
        .message("Lấy thống kê kho hàng thành công")
        .data(stockService.getDashboard())
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/products-stock")
  public ApiResp<Page<com.graduation.project.inventory.dto.resp.WarehouseStockResp>>
      getWarehouseProductsStock(
          @RequestParam(required = false, defaultValue = "0") int page,
          @RequestParam(required = false, defaultValue = "1000") int size) {
    return ApiResp.<Page<com.graduation.project.inventory.dto.resp.WarehouseStockResp>>builder()
        .message("Lấy tồn kho bảo quản thành công")
        .data(stockService.getWarehouseStock(page, size))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Vouchers =====

  @GetMapping("/vouchers")
  public ApiResp<Page<StockVoucherResp>> getVouchers(
      @RequestParam(required = false) VoucherType type,
      @RequestParam(required = false) VoucherStatus status,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "20") int size) {
    return ApiResp.<Page<StockVoucherResp>>builder()
        .message("Lấy danh sách phiếu kho thành công")
        .data(stockService.getVouchers(type, status, page, size))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/vouchers/{id}")
  public ApiResp<StockVoucherResp> getVoucherById(@PathVariable UUID id) {
    return ApiResp.<StockVoucherResp>builder()
        .message("Lấy phiếu kho thành công")
        .data(stockService.getVoucherById(id))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PostMapping("/vouchers")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResp<StockVoucherResp> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
    return ApiResp.<StockVoucherResp>builder()
        .message("Tạo phiếu kho thành công")
        .data(stockService.createVoucher(request))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/vouchers/{id}/approve")
  public ApiResp<StockVoucherResp> approveVoucher(
      @PathVariable UUID id, Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);
    return ApiResp.<StockVoucherResp>builder()
        .message("Duyệt phiếu kho thành công")
        .data(stockService.approveVoucher(id, currentUserId))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/vouchers/{id}/cancel")
  public ApiResp<StockVoucherResp> cancelVoucher(@PathVariable UUID id) {
    return ApiResp.<StockVoucherResp>builder()
        .message("Hủy phiếu kho thành công")
        .data(stockService.cancelVoucher(id))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Batches =====

  @GetMapping("/batches")
  public ApiResp<List<StockBatchResp>> getBatches(
      @RequestParam(required = false, defaultValue = "STORAGE") WarehouseLocation warehouse) {
    return ApiResp.<List<StockBatchResp>>builder()
        .message("Lấy danh sách lô hàng theo kho thành công")
        .data(stockService.getBatchesByWarehouse(warehouse))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/batches/medicine/{medicineId}")
  public ApiResp<List<StockBatchResp>> getBatchesByMedicine(@PathVariable UUID medicineId) {
    return ApiResp.<List<StockBatchResp>>builder()
        .message("Lấy lô hàng theo thuốc/vật tư thành công")
        .data(stockService.getBatchesByMedicine(medicineId))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/batches/product/{productId}")
  @PreAuthorize(
      "hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAREHOUSE', 'ROLE_DOCTOR', 'ROLE_SHOP_STAFF')")
  public ApiResp<List<StockBatchResp>> getBatchesByProduct(@PathVariable UUID productId) {
    return ApiResp.<List<StockBatchResp>>builder()
        .message("Lấy lô hàng theo sản phẩm thành công")
        .data(stockService.getBatchesByProduct(productId))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Alerts =====

  @GetMapping("/alerts/near-expiry")
  public ApiResp<List<StockBatchResp>> getNearExpiryBatches(
      @RequestParam(required = false, defaultValue = "STORAGE") WarehouseLocation warehouse) {
    return ApiResp.<List<StockBatchResp>>builder()
        .message("Lấy danh sách lô cận date thành công")
        .data(stockService.getNearExpiryBatches(warehouse))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/alerts/expired")
  public ApiResp<List<StockBatchResp>> getExpiredBatches(
      @RequestParam(required = false, defaultValue = "STORAGE") WarehouseLocation warehouse) {
    return ApiResp.<List<StockBatchResp>>builder()
        .message("Lấy danh sách lô hết hạn thành công")
        .data(stockService.getExpiredBatches(warehouse))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Quick Export Expired Batches to Doctor Warehouse =====

  @PostMapping("/batches/{batchId}/export-to-doctor")
  public ApiResp<StockVoucherResp> exportExpiredBatchToDoctor(
      @PathVariable UUID batchId, Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);
    return ApiResp.<StockVoucherResp>builder()
        .message("Xuất lô hết date lên kho bác sĩ thành công")
        .data(stockService.exportExpiredBatchToDoctor(batchId, currentUserId))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PostMapping("/batches/export-all-expired-to-doctor")
  public ApiResp<StockVoucherResp> exportAllExpiredBatchesToDoctor(Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);
    return ApiResp.<StockVoucherResp>builder()
        .message("Xuất tất cả lô hết date lên kho bác sĩ thành công")
        .data(stockService.exportAllExpiredBatchesToDoctor(currentUserId))
        .timestamp(Instant.now().toString())
        .build();
  }
}
