package com.graduation.project.product.dto.req;

import com.graduation.project.product.entity.Product.PetType;
import java.math.BigDecimal;
import java.util.List;

public record ProductFilterRequest(
    String search,
    List<String> categorySlugs,
    List<String> brandSlugs,
    List<PetType> petTypes,
    BigDecimal priceMin,
    BigDecimal priceMax,
    Boolean inStockOnly,
    String sort, // 'featured' | 'price-asc' | 'price-desc' | 'rating-desc' | 'newest'
    Integer page, // default 0
    Integer size // default 12
    ) {
  public ProductFilterRequest {
    if (page == null || page < 0) page = 0;
    if (size == null || size <= 0 || size > 100) size = 12;
    if (sort == null || sort.isBlank()) sort = "featured";
  }
}
