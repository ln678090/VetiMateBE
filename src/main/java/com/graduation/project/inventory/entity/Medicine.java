package com.graduation.project.inventory.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@NoArgsConstructor
public class Medicine {
  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(unique = true, length = 50)
  private String sku;

  @Column(nullable = false, length = 30)
  private String unit;

  @Column(name = "min_stock", nullable = false, precision = 10, scale = 2)
  private BigDecimal minStock = BigDecimal.ZERO;
  @Column(name = "import_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal importPrice = BigDecimal.ZERO;

  @Column(name = "sell_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal sellPrice = BigDecimal.ZERO;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
