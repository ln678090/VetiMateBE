package com.graduation.project.user.service.Impl;

import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.mapper.ProductMapper;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.user.entity.UserFavoriteProduct;
import com.graduation.project.user.entity.UserFavoriteProduct.UserFavoriteProductId;
import com.graduation.project.user.entity.UserViewedProduct;
import com.graduation.project.user.entity.UserViewedProduct.UserViewedProductId;
import com.graduation.project.user.repository.UserFavoriteProductRepository;
import com.graduation.project.user.repository.UserViewedProductRepository;
import com.graduation.project.user.service.ProductInteractionService;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductInteractionServiceImpl implements ProductInteractionService {

  private final UserFavoriteProductRepository favoriteRepository;
  private final UserViewedProductRepository viewedRepository;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Override
  @Transactional(readOnly = true)
  public boolean isFavorite(UUID userId, UUID productId) {
    return favoriteRepository.existsById(new UserFavoriteProductId(userId, productId));
  }

  @Override
  @Transactional
  public void toggleFavorite(UUID userId, UUID productId) {
    UserFavoriteProductId id = new UserFavoriteProductId(userId, productId);
    if (favoriteRepository.existsById(id)) {
      favoriteRepository.deleteById(id);
    } else {
      Product product =
          productRepository
              .findById(productId)
              .orElseThrow(() -> new IllegalArgumentException("Product not found"));
      UserFavoriteProduct favorite =
          UserFavoriteProduct.builder().userId(userId).productId(productId).build();
      favoriteRepository.save(favorite);
    }
  }

  @Override
  @Transactional
  public void recordView(UUID userId, UUID productId) {
    UserViewedProductId id = new UserViewedProductId(userId, productId);
    UserViewedProduct viewed =
        viewedRepository
            .findById(id)
            .orElseGet(
                () -> {
                  Product product =
                      productRepository
                          .findById(productId)
                          .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                  return UserViewedProduct.builder().userId(userId).productId(productId).build();
                });
    viewed.setViewedAt(OffsetDateTime.now());
    viewedRepository.save(viewed);
    viewedRepository.keepTopN(userId, 50);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResp> getFavoriteProducts(
      UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
    if (startDate != null && endDate != null) {
      return favoriteRepository
          .findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate, pageable)
          .map(fav -> productMapper.toResp(fav.getProduct()));
    }
    return favoriteRepository
        .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        .map(fav -> productMapper.toResp(fav.getProduct()));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ProductResp> getRecentlyViewedProducts(
      UUID userId, OffsetDateTime startDate, OffsetDateTime endDate, Pageable pageable) {
    if (startDate != null && endDate != null) {
      return viewedRepository
          .findByUserIdAndViewedAtBetweenOrderByViewedAtDesc(userId, startDate, endDate, pageable)
          .map(viewed -> productMapper.toResp(viewed.getProduct()));
    }
    return viewedRepository
        .findByUserIdOrderByViewedAtDesc(userId, pageable)
        .map(viewed -> productMapper.toResp(viewed.getProduct()));
  }
}
