package com.graduation.project.inventory.service.Impl;

import com.graduation.project.inventory.dto.req.CreateImportVoucherReq;
import com.graduation.project.inventory.dto.req.ImportVoucherItemReq;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.entity.StockBatch;
import com.graduation.project.inventory.entity.StockVoucher;
import com.graduation.project.inventory.entity.StockVoucher.VoucherStatus;
import com.graduation.project.inventory.entity.StockVoucher.VoucherType;
import com.graduation.project.inventory.entity.StockVoucherItem;
import com.graduation.project.inventory.entity.Supplier;
import com.graduation.project.inventory.repository.StockBatchRepository;
import com.graduation.project.inventory.repository.StockVoucherRepository;
import com.graduation.project.inventory.repository.SupplierRepository;
import com.graduation.project.inventory.service.InventoryService;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.repository.StaffRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryServiceImpl implements InventoryService {

  private final StockVoucherRepository voucherRepository;
  private final StockBatchRepository batchRepository;
  private final SupplierRepository supplierRepository;
  private final ProductRepository productRepository;
  private final StaffRepository staffRepository;

  @Override
  @Transactional
  public StockVoucherResp createImportVoucher(CreateImportVoucherReq req, UUID createdByUserId) {
    Staff creator =
        staffRepository
            .findByUserIdAndActiveTrue(createdByUserId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

    StockVoucher voucher =
        StockVoucher.builder()
            .type(VoucherType.IMPORT)
            .status(VoucherStatus.DRAFT)
            .createdBy(creator)
            .note(req.getNote())
            .build();

    for (ImportVoucherItemReq itemReq : req.getItems()) {
      Product product = null;
      if (itemReq.getProductId() != null) {
        product =
            productRepository
                .findById(itemReq.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
      }

      Supplier supplier = null;
      if (itemReq.getSupplierId() != null) {
        supplier =
            supplierRepository
                .findById(itemReq.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp"));
      }

      if (product == null && itemReq.getMedicineId() == null) {
        throw new IllegalArgumentException("Phải chọn sản phẩm hoặc thuốc");
      }

      StockVoucherItem item =
          StockVoucherItem.builder()
              .product(product)
              .quantity(itemReq.getQuantity())
              .unitPrice(itemReq.getImportPrice())
              .note(itemReq.getNote())
              .build();

      voucher.addItem(item);
    }

    voucher = voucherRepository.save(voucher);
    return mapToResp(voucher);
  }

  @Override
  @Transactional
  public StockVoucherResp approveVoucher(UUID voucherId, UUID approvedByUserId) {
    StockVoucher voucher =
        voucherRepository
            .findById(voucherId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu"));

    if (voucher.getStatus() != VoucherStatus.DRAFT) {
      throw new IllegalStateException("Chỉ có thể duyệt phiếu ở trạng thái NHÁP");
    }

    Staff approver =
        staffRepository
            .findByUserIdAndActiveTrue(approvedByUserId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên duyệt"));

    voucher.setStatus(VoucherStatus.APPROVED);
    voucher.setApprovedBy(approver);
    voucher.setApprovedAt(OffsetDateTime.now());

    // Update stock and create batches
    if (voucher.getType() == VoucherType.IMPORT) {
      for (StockVoucherItem item : voucher.getItems()) {
        StockBatch batch =
            StockBatch.builder()
                .product(item.getProduct())
                .medicine(item.getMedicine())
                .quantity(item.getQuantity())
                .remainingQty(item.getQuantity())
                .importPrice(item.getUnitPrice())
                .build();

        batch = batchRepository.save(batch);
        item.setBatch(batch);

        if (item.getProduct() != null) {
          Product p = item.getProduct();
          p.setStockQuantity(p.getStockQuantity() + item.getQuantity().intValue());
          productRepository.save(p);
        }
      }
    }

    return mapToResp(voucher);
  }

  @Override
  public List<StockVoucherResp> getAllVouchers() {
    return voucherRepository.findAll().stream().map(this::mapToResp).collect(Collectors.toList());
  }

  public List<Supplier> getAllSuppliers() {
    return supplierRepository.findAll();
  }

  private StockVoucherResp mapToResp(StockVoucher v) {
    return StockVoucherResp.builder()
        .id(v.getId())
        .type(v.getType().name())
        .status(v.getStatus().name())
        .createdBy(v.getCreatedBy() != null ? v.getCreatedBy().getFullName() : null)
        .approvedBy(v.getApprovedBy() != null ? v.getApprovedBy().getFullName() : null)
        .note(v.getNote())
        .createdAt(v.getCreatedAt() != null ? v.getCreatedAt().toString() : null)
        .approvedAt(v.getApprovedAt() != null ? v.getApprovedAt().toString() : null)
        .build();
  }
}
