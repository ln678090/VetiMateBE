package com.graduation.project.loyalty.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DiscountType discountType;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal discountValue;

  @Column(precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal minOrderAmount = BigDecimal.ZERO;

  @Column(precision = 10, scale = 2)
  private BigDecimal maxDiscount;

  @Column(nullable = false)
  @Builder.Default
  private Integer pointsRequired = 0;

  private Integer usageLimit;

  @Builder.Default private Integer usedCount = 0;

  private LocalDateTime startDate;
  private LocalDateTime endDate;

  @Builder.Default private Boolean isActive = true;

  @Enumerated(EnumType.STRING)
  @Column(name = "required_tier", length = 20)
  private CustomerTier requiredTier;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;
}
