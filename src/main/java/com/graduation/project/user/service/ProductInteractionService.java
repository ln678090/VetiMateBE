package com.graduation.project.user.service;

import com.graduation.project.product.dto.resp.ProductResp;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductInteractionService {
  boolean isFavorite(UUID userId, UUID productId);

  void toggleFavorite(UUID userId, UUID productId);

  void recordView(UUID userId, UUID productId);

  Page<ProductResp> getFavoriteProducts(
      UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);

  Page<ProductResp> getRecentlyViewedProducts(
      UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable);
}
