package com.graduation.project.product.entity;

import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "products")
public class Product {

  @Id
  @UuidV7
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(nullable = false, length = 255)
  private String name;

  @Column(nullable = false, length = 255, unique = true)
  private String slug;

  @Column(length = 50, unique = true)
  private String sku;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "short_desc", length = 500)
  private String shortDesc;

  // ===== Relations =====
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "brand_id", nullable = false)
  private Brand brand;

  @Enumerated(EnumType.STRING)
  @Column(name = "pet_type", nullable = false, length = 10)
  @Builder.Default
  private PetType petType = PetType.both;

  // ===== Pricing & stock =====
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  @Column(name = "original_price", precision = 12, scale = 2)
  private BigDecimal originalPrice;

  @Column(name = "stock_quantity", nullable = false)
  @Builder.Default
  private Integer stockQuantity = 0;

  // ===== Rating =====
  @Column(nullable = false, precision = 2, scale = 1)
  @Builder.Default
  private BigDecimal rating = BigDecimal.ZERO;

  @Column(name = "review_count", nullable = false)
  @Builder.Default
  private Integer reviewCount = 0;

  // ===== Media =====
  @Column(name = "image_url", nullable = false, length = 500)
  private String imageUrl;

  @Column(name = "gallery_urls", columnDefinition = "TEXT")
  private String galleryUrls; // JSON array stored as text

  // ===== Flags =====
  @Column(name = "is_featured", nullable = false)
  @Builder.Default
  private Boolean isFeatured = false;

  @Column(name = "is_new", nullable = false)
  @Builder.Default
  private Boolean isNew = false;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  /** Convenience: check còn hàng */
  public boolean isInStock() {
    return stockQuantity != null && stockQuantity > 0;
  }

  public enum PetType {
    dog,
    cat,
    both
  }
}
