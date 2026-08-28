package com.graduation.project.user.repository;

import com.graduation.project.user.entity.UserViewedProduct;
import com.graduation.project.user.entity.UserViewedProduct.UserViewedProductId;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserViewedProductRepository extends JpaRepository<UserViewedProduct, UserViewedProductId> {
    Page<UserViewedProduct> findByUserIdOrderByViewedAtDesc(UUID userId, Pageable pageable);
    Page<UserViewedProduct> findByUserIdAndViewedAtBetweenOrderByViewedAtDesc(UUID userId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);

    @Modifying
    @Query(value = "DELETE FROM user_viewed_products WHERE user_id = :userId AND product_id NOT IN " +
                   "(SELECT product_id FROM user_viewed_products WHERE user_id = :userId ORDER BY viewed_at DESC LIMIT :limit)", 
           nativeQuery = true)
    void keepTopN(@Param("userId") UUID userId, @Param("limit") int limit);
}
