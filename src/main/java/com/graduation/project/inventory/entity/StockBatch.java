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
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
@Table(name = "stock_batches")
public class StockBatch {

  @Id
  @UuidV7
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "medicine_id")
  private Medicine medicine;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @Column(name = "batch_code", length = 100)
  private String batchCode;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantity;

  @Column(name = "remaining_qty", nullable = false, precision = 10, scale = 2)
  private BigDecimal remainingQty;

  @Column(name = "import_price", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal importPrice = BigDecimal.ZERO;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @Column(name = "received_at", nullable = false)
  @Builder.Default
  private OffsetDateTime receivedAt = OffsetDateTime.now();

  /** Kiểm tra lô hàng đã hết chưa */
  public boolean isExhausted() {
    return remainingQty != null && remainingQty.compareTo(BigDecimal.ZERO) <= 0;
  }

  /** Kiểm tra lô hàng đã hết hạn chưa */
  public boolean isExpired() {
    return expiryDate != null && expiryDate.isBefore(LocalDate.now());
  }

  /** Kiểm tra lô hàng sắp hết hạn (trong N ngày tới) */
  public boolean isNearExpiry(int days) {
    return expiryDate != null
        && !isExpired()
        && expiryDate.isBefore(LocalDate.now().plusDays(days));
  }
}
