package com.graduation.project.product.dto.resp;

import com.graduation.project.product.entity.Product.PetType;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResp(
    UUID id,
    String name,
    String slug,
    String sku,
    String description,
    String shortDesc,
    UUID categoryId,
    String categoryName,
    String categorySlug,
    UUID brandId,
    String brandName,
    String brandSlug,
    PetType petType,
    BigDecimal price,
    BigDecimal originalPrice,
    Integer stockQuantity,
    Boolean inStock,
    BigDecimal rating,
    Integer reviewCount,
    String imageUrl,
    String galleryUrls,
    Boolean isFeatured,
    Boolean isNew,
    Boolean isActive) {}
