package com.graduation.project.user.repository;

import com.graduation.project.user.entity.UserFavoriteProduct;
import com.graduation.project.user.entity.UserFavoriteProduct.UserFavoriteProductId;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteProductRepository extends JpaRepository<UserFavoriteProduct, UserFavoriteProductId> {
    Page<UserFavoriteProduct> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<UserFavoriteProduct> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID userId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);
    boolean existsByUserIdAndProductId(UUID userId, UUID productId);
}
