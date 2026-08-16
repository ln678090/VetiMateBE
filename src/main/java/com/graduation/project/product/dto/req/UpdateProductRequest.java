package com.graduation.project.product.dto.req;

import com.graduation.project.product.entity.Product.PetType;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
    String name,
    String sku,
    String description,
    String shortDesc,
    UUID categoryId,
    UUID brandId,
    PetType petType,
    @Min(0) BigDecimal price,
    BigDecimal originalPrice,
    @Min(0) Integer stockQuantity,
    String imageUrl,
    String galleryUrls,
    Boolean isFeatured,
    Boolean isNew,
    Boolean isActive) {}
