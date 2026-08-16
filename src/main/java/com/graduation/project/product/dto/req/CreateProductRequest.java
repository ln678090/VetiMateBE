package com.graduation.project.product.dto.req;

import com.graduation.project.product.entity.Product.PetType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
    @NotBlank(message = "Tên sản phẩm không được để trống") String name,
    String sku,
    String description,
    String shortDesc,
    @NotNull(message = "Danh mục không được để trống") UUID categoryId,
    @NotNull(message = "Thương hiệu không được để trống") UUID brandId,
    @NotNull(message = "Loại thú cưng không được để trống") PetType petType,
    @NotNull(message = "Giá không được để trống") @Min(0) BigDecimal price,
    BigDecimal originalPrice,
    @NotNull @Min(0) Integer stockQuantity,
    @NotBlank(message = "Ảnh không được để trống") String imageUrl,
    String galleryUrls,
    Boolean isFeatured,
    Boolean isNew) {}
