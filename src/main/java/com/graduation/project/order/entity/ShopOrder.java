package com.graduation.project.order.entity;

import com.graduation.project.user.entity.User;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shop_orders")
public class ShopOrder {

  @Id
  @UuidV7
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "order_code", nullable = false, unique = true, length = 50)
  private String orderCode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private OrderStatus status = OrderStatus.PENDING;

  @Column(nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "shipping_fee", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Column(name = "cancellation_requested", nullable = false)
  @Builder.Default
  private boolean cancellationRequested = false;

  @Column(name = "cancellation_reason", length = 255)
  private String cancellationReason;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal totalAmount = BigDecimal.ZERO;

  @Column(name = "payment_method", nullable = false, length = 30)
  @Builder.Default
  private String paymentMethod = "COD";

  @Column(name = "recipient_name", nullable = false, length = 150)
  private String recipientName;

  @Column(name = "recipient_phone", nullable = false, length = 20)
  private String recipientPhone;

  @Column(name = "shipping_address", nullable = false, length = 500)
  private String shippingAddress;

  @Column(length = 500)
  private String note;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ShopOrderItem> items = new ArrayList<>();

  public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    SHIPPING,
    COMPLETED,
    CANCELLED
  }
}
