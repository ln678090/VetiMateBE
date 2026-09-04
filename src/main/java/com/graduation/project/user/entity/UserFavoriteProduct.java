package com.graduation.project.user.entity;

import com.graduation.project.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_favorite_products")
@IdClass(UserFavoriteProduct.UserFavoriteProductId.class)
public class UserFavoriteProduct {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @Id
  @Column(name = "product_id")
  private UUID productId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", insertable = false, updatable = false)
  private Product product;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserFavoriteProductId implements Serializable {
    private UUID userId;
    private UUID productId;
  }
}
