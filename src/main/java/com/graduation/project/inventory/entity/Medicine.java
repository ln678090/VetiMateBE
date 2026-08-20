package com.graduation.project.inventory.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
@Table(name = "medicines")
public class Medicine {

  @Id
  @UuidV7
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(unique = true, length = 50)
  private String sku;

  @Column(nullable = false, length = 30)
  private String unit;

  @Column(name = "min_stock", nullable = false, precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal minStock = BigDecimal.ZERO;

  @Column(name = "import_price", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal importPrice = BigDecimal.ZERO;

  @Column(name = "sell_price", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal sellPrice = BigDecimal.ZERO;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
