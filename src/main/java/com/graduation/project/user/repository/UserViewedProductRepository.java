package com.graduation.project.user.repository;

import com.graduation.project.user.entity.UserViewedProduct;
import com.graduation.project.user.entity.UserViewedProduct.UserViewedProductId;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserViewedProductRepository extends JpaRepository<UserViewedProduct, UserViewedProductId> {
    Page<UserViewedProduct> findByUserIdOrderByViewedAtDesc(UUID userId, Pageable pageable);
    Page<UserViewedProduct> findByUserIdAndViewedAtBetweenOrderByViewedAtDesc(UUID userId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);
}
