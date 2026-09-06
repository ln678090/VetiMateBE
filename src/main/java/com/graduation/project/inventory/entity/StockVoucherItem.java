package com.graduation.project.inventory.entity;

import com.graduation.project.product.entity.Product;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stock_voucher_items")
public class StockVoucherItem {

  @Id
  @UuidV7
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "voucher_id", nullable = false)
  private StockVoucher voucher;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "batch_id")
  private StockBatch batch;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medicine_id")
  private Medicine medicine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantity;

  @Column(name = "unit_price", precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "batch_code", length = 100)
  private String batchCode;

  @Column(name = "expiry_date")
  private java.time.LocalDate expiryDate;

  @Column(length = 255)
  private String note;

  /** Lấy tên hiển thị — thuốc hoặc sản phẩm */
  public String getItemName() {
    if (medicine != null) return medicine.getName();
    if (product != null) return product.getName();
    return "N/A";
  }
}
