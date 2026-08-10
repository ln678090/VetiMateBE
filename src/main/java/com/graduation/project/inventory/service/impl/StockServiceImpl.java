package com.graduation.project.inventory.service.impl;

import com.graduation.project.inventory.dto.req.CreateVoucherRequest;
import com.graduation.project.inventory.dto.req.VoucherItemRequest;
import com.graduation.project.inventory.dto.resp.InventoryDashboardResp;
import com.graduation.project.inventory.dto.resp.StockBatchResp;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.inventory.entity.StockBatch;
import com.graduation.project.inventory.entity.StockVoucher;
import com.graduation.project.inventory.entity.StockVoucherItem;
import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import com.graduation.project.inventory.mapper.InventoryMapper;
import com.graduation.project.inventory.repository.MedicineRepository;
import com.graduation.project.inventory.repository.StockBatchRepository;
import com.graduation.project.inventory.repository.StockVoucherItemRepository;
import com.graduation.project.inventory.repository.StockVoucherRepository;
import com.graduation.project.inventory.repository.SupplierRepository;
import com.graduation.project.inventory.service.StockService;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

  private final StockVoucherRepository voucherRepository;
  private final StockVoucherItemRepository voucherItemRepository;
  private final StockBatchRepository batchRepository;
  private final MedicineRepository medicineRepository;
  private final ProductRepository productRepository;
  private final SupplierRepository supplierRepository;
  private final InventoryMapper inventoryMapper;

  // ============================================================
  // VOUCHER CRUD
  // ============================================================

  @Override
  @Transactional
  public StockVoucherResp createVoucher(CreateVoucherRequest request) {
    StockVoucher voucher =
        StockVoucher.builder()
            .type(request.type())
            .status(VoucherStatus.DRAFT)
            .note(request.note())
            .build();
    voucher = voucherRepository.save(voucher);

    List<StockVoucherItem> items = new ArrayList<>();
    for (VoucherItemRequest itemReq : request.items()) {
      StockVoucherItem item = buildVoucherItem(voucher, itemReq);
      items.add(item);
    }
    voucherItemRepository.saveAll(items);
    voucher.setItems(items);

    log.info("Tạo phiếu kho {} loại {} với {} dòng", voucher.getId(), request.type(), items.size());
    return inventoryMapper.toVoucherResp(voucher);
  }

  @Override
  @Transactional
  public StockVoucherResp approveVoucher(UUID voucherId) {
    StockVoucher voucher = findVoucherOrThrow(voucherId);

    if (voucher.getStatus() != VoucherStatus.DRAFT) {
      throw new IllegalArgumentException(
          "Chỉ có thể duyệt phiếu ở trạng thái DRAFT. Trạng thái hiện tại: "
              + voucher.getStatus());
    }

    // Load items
    List<StockVoucherItem> items = voucherItemRepository.findByVoucherIdOrderById(voucherId);

    switch (voucher.getType()) {
      case IMPORT -> processImport(items);
      case EXPORT -> processExport(items);
      case TRANSFER -> {
        /* transfer giữa kho — tạm bỏ qua cho MVP */
      }
      case STOCKTAKE -> processStocktake(items);
    }

    voucher.setStatus(VoucherStatus.APPROVED);
    voucher.setApprovedAt(OffsetDateTime.now());
    voucherRepository.save(voucher);

    voucher.setItems(items);
    log.info("Duyệt phiếu kho {} thành công", voucherId);
    return inventoryMapper.toVoucherResp(voucher);
  }

  @Override
  @Transactional
  public StockVoucherResp cancelVoucher(UUID voucherId) {
    StockVoucher voucher = findVoucherOrThrow(voucherId);

    if (voucher.getStatus() != VoucherStatus.DRAFT) {
      throw new IllegalArgumentException(
          "Chỉ có thể hủy phiếu ở trạng thái DRAFT. Trạng thái hiện tại: "
              + voucher.getStatus());
    }

    voucher.setStatus(VoucherStatus.CANCELLED);
    voucherRepository.save(voucher);

    List<StockVoucherItem> items = voucherItemRepository.findByVoucherIdOrderById(voucherId);
    voucher.setItems(items);
    return inventoryMapper.toVoucherResp(voucher);
  }

  @Override
  public StockVoucherResp getVoucherById(UUID id) {
    StockVoucher voucher = findVoucherOrThrow(id);
    List<StockVoucherItem> items = voucherItemRepository.findByVoucherIdOrderById(id);
    voucher.setItems(items);
    return inventoryMapper.toVoucherResp(voucher);
  }

  @Override
  public Page<StockVoucherResp> getVouchers(
      VoucherType type, VoucherStatus status, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<StockVoucher> result;

    if (type != null && status != null) {
      result = voucherRepository.findByTypeAndStatusOrderByCreatedAtDesc(type, status, pageable);
    } else if (type != null) {
      result = voucherRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    } else if (status != null) {
      result = voucherRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    } else {
      result = voucherRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    return result.map(v -> {
      List<StockVoucherItem> items = voucherItemRepository.findByVoucherIdOrderById(v.getId());
      v.setItems(items);
      return inventoryMapper.toVoucherResp(v);
    });
  }

  // ============================================================
  // BATCH QUERIES
  // ============================================================

  @Override
  public List<StockBatchResp> getBatchesByMedicine(UUID medicineId) {
    return inventoryMapper.toBatchRespList(
        batchRepository.findByMedicineIdOrderByReceivedAtDesc(medicineId));
  }

  @Override
  public List<StockBatchResp> getBatchesByProduct(UUID productId) {
    return inventoryMapper.toBatchRespList(
        batchRepository.findByProductIdOrderByReceivedAtDesc(productId));
  }

  @Override
  public List<StockBatchResp> getNearExpiryBatches() {
    LocalDate threshold = LocalDate.now().plusDays(30);
    return inventoryMapper.toBatchRespList(batchRepository.findNearExpiryBatches(threshold));
  }

  @Override
  public List<StockBatchResp> getExpiredBatches() {
    return inventoryMapper.toBatchRespList(batchRepository.findExpiredBatches());
  }

  // ============================================================
  // DASHBOARD
  // ============================================================

  @Override
  public InventoryDashboardResp getDashboard() {
    long totalMedicines = medicineRepository.count();
    long totalSuppliers = supplierRepository.count();
    long lowStockCount = medicineRepository.findLowStockMedicines().size();
    long nearExpiryCount =
        batchRepository.findNearExpiryBatches(LocalDate.now().plusDays(30)).size();
    long expiredCount = batchRepository.findExpiredBatches().size();
    long pendingVouchers = voucherRepository.countByStatus(VoucherStatus.DRAFT);

    // Tổng giá trị tồn kho = sum(remaining_qty * import_price) cho tất cả batch
    BigDecimal totalStockValue =
        batchRepository.findAll().stream()
            .filter(b -> b.getRemainingQty().compareTo(BigDecimal.ZERO) > 0)
            .map(b -> b.getRemainingQty().multiply(b.getImportPrice()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new InventoryDashboardResp(
        totalMedicines,
        totalSuppliers,
        lowStockCount,
        nearExpiryCount,
        expiredCount,
        pendingVouchers,
        totalStockValue);
  }

  // ============================================================
  // PRIVATE: IMPORT / EXPORT / STOCKTAKE LOGIC
  // ============================================================

  /** Nhập kho: tạo batch mới cho mỗi dòng item */
  private void processImport(List<StockVoucherItem> items) {
    for (StockVoucherItem item : items) {
      StockBatch batch =
          StockBatch.builder()
              .medicine(item.getMedicine())
              .product(item.getProduct())
              .quantity(item.getQuantity())
              .remainingQty(item.getQuantity())
              .importPrice(item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO)
              .batchCode(item.getNote()) // tạm dùng note chứa batchCode
              .receivedAt(OffsetDateTime.now())
              .build();
      batch = batchRepository.save(batch);
      item.setBatch(batch);
      voucherItemRepository.save(item);
    }
  }

  /** Xuất kho: trừ remaining_qty từ batch được chỉ định, FEFO nếu không chỉ định batch */
  private void processExport(List<StockVoucherItem> items) {
    for (StockVoucherItem item : items) {
      BigDecimal qtyToExport = item.getQuantity();

      if (item.getBatch() != null) {
        // Xuất từ batch cụ thể
        StockBatch batch = item.getBatch();
        if (batch.getRemainingQty().compareTo(qtyToExport) < 0) {
          throw new IllegalArgumentException(
              "Lô " + batch.getBatchCode() + " không đủ tồn. Còn: " + batch.getRemainingQty());
        }
        batch.setRemainingQty(batch.getRemainingQty().subtract(qtyToExport));
        batchRepository.save(batch);
      } else {
        // FEFO: xuất từ lô sắp hết hạn trước
        List<StockBatch> batches;
        if (item.getMedicine() != null) {
          batches =
              batchRepository.findAvailableBatchesByMedicineFefo(item.getMedicine().getId());
        } else if (item.getProduct() != null) {
          batches =
              batchRepository.findAvailableBatchesByProductFefo(item.getProduct().getId());
        } else {
          throw new IllegalArgumentException("Dòng xuất kho phải có medicine hoặc product");
        }

        BigDecimal remaining = qtyToExport;
        for (StockBatch batch : batches) {
          if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

          BigDecimal canTake = batch.getRemainingQty().min(remaining);
          batch.setRemainingQty(batch.getRemainingQty().subtract(canTake));
          batchRepository.save(batch);
          remaining = remaining.subtract(canTake);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
          String itemName = item.getItemName();
          throw new IllegalArgumentException(
              "Không đủ tồn kho cho " + itemName + ". Thiếu: " + remaining);
        }
      }
    }
  }

  /** Kiểm kê: điều chỉnh remaining_qty của batch về số thực tế */
  private void processStocktake(List<StockVoucherItem> items) {
    for (StockVoucherItem item : items) {
      if (item.getBatch() == null) {
        throw new IllegalArgumentException("Kiểm kê phải chỉ định lô hàng cụ thể");
      }
      StockBatch batch = item.getBatch();
      batch.setRemainingQty(item.getQuantity());
      batchRepository.save(batch);
    }
  }

  // ============================================================
  // HELPERS
  // ============================================================

  private StockVoucherItem buildVoucherItem(
      StockVoucher voucher, VoucherItemRequest req) {
    Medicine medicine = null;
    Product product = null;
    StockBatch batch = null;

    if (req.medicineId() != null) {
      medicine =
          medicineRepository
              .findById(req.medicineId())
              .orElseThrow(
                  () -> new NoSuchElementException("Không tìm thấy thuốc: " + req.medicineId()));
    }
    if (req.productId() != null) {
      product =
          productRepository
              .findById(req.productId())
              .orElseThrow(
                  () ->
                      new NoSuchElementException("Không tìm thấy sản phẩm: " + req.productId()));
    }
    if (req.batchId() != null) {
      batch =
          batchRepository
              .findById(req.batchId())
              .orElseThrow(
                  () -> new NoSuchElementException("Không tìm thấy lô hàng: " + req.batchId()));
    }

    if (medicine == null && product == null) {
      throw new IllegalArgumentException("Mỗi dòng phải có thuốc hoặc sản phẩm");
    }

    return StockVoucherItem.builder()
        .voucher(voucher)
        .medicine(medicine)
        .product(product)
        .batch(batch)
        .quantity(req.quantity())
        .unitPrice(req.unitPrice())
        .note(req.note())
        .build();
  }

  private StockVoucher findVoucherOrThrow(UUID id) {
    return voucherRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phiếu kho: " + id));
  }
}
