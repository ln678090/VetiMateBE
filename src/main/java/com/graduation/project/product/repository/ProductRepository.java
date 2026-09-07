package com.graduation.project.product.repository;

import com.graduation.project.product.entity.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

  Optional<Product> findBySlugAndIsActiveTrue(String slug);

  /** Related products: cùng category, khác slug, còn active, sort theo rating */
  @Query(
      """
            SELECT p FROM Product p
            WHERE p.category.id = :categoryId
              AND p.slug <> :excludeSlug
              AND p.isActive = true
              AND p.stockQuantity > 0
            ORDER BY p.rating DESC, p.reviewCount DESC
            """)
  List<Product> findRelatedProducts(
      @Param("categoryId") UUID categoryId,
      @Param("excludeSlug") String excludeSlug,
      Pageable pageable);

  /** Featured products cho landing page */
  Page<Product> findByIsFeaturedTrueAndIsActiveTrueOrderByRatingDesc(Pageable pageable);

  boolean existsBySlug(String slug);

  @Query(
      """
            SELECT new com.graduation.project.inventory.dto.resp.WarehouseStockResp(
                p.id, p.name, p.category.name, p.brand.name, SUM(b.remainingQty)
            )
            FROM Product p
            LEFT JOIN StockBatch b ON p.id = b.product.id
            GROUP BY p.id, p.name, p.category.name, p.brand.name
            ORDER BY p.createdAt DESC
            """)
  Page<com.graduation.project.inventory.dto.resp.WarehouseStockResp> findAllWarehouseStock(
      Pageable pageable);
}
