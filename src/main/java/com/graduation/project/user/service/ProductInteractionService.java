package com.graduation.project.user.service;

import com.graduation.project.product.dto.resp.ProductResp;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductInteractionService {
    void toggleFavorite(UUID userId, UUID productId);
    void recordView(UUID userId, UUID productId);
    Page<ProductResp> getFavoriteProducts(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);
    Page<ProductResp> getRecentlyViewedProducts(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);
}
