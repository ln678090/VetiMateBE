package com.graduation.project.product.dto.req;

import com.graduation.project.product.entity.Product.PetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReq {
  @NotBlank(message = "Tên sản phẩm không được để trống")
  private String name;

  private String description;
  private String shortDesc;

  @NotNull(message = "Danh mục không được để trống")
  private UUID categoryId;

  @NotNull(message = "Thương hiệu không được để trống")
  private UUID brandId;

  @NotNull(message = "Pet type không được để trống")
  private PetType petType;

  @NotNull(message = "Giá không được để trống")
  private BigDecimal price;

  private BigDecimal originalPrice;

  @Builder.Default private Integer stockQuantity = 0;

  @NotBlank(message = "URL ảnh không được để trống")
  private String imageUrl;

  private String galleryUrls;

  @Builder.Default private Boolean isFeatured = false;

  @Builder.Default private Boolean isNew = false;

  @Builder.Default private Boolean isActive = true;
}
