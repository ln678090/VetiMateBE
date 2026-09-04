package com.graduation.project.clinic.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

  @Id
  @UuidV7
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "invoice_code", nullable = false, length = 50, unique = true)
  private String invoiceCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pet_id")
  private Pet pet;

  @Column(name = "parent_invoice_id")
  private UUID parentInvoiceId;

  @Column(name = "type", nullable = false, length = 20)
  private String type; // 'CLINIC', 'SHOP', 'MIXED'

  @Column(name = "status", nullable = false, length = 20)
  private String status; // 'DRAFT', 'PAID', 'CANCELLED'

  @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "payment_method", length = 30)
  private String paymentMethod; // 'CASH', 'CARD', 'BANK_TRANSFER', 'VNPAY', 'MOMO'

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "note", length = 500)
  private String note;

  @OneToMany(
      mappedBy = "invoice",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @Builder.Default
  private List<InvoiceItem> items = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_reviewed")
  @Builder.Default
  private Boolean isReviewed = false;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
