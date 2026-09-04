package com.graduation.project.product.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.entity.Product.PetType;
import com.graduation.project.product.service.ProductService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import java.util.UUID;
import com.graduation.project.product.dto.req.ProductReq;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  /**
   * GET /api/products?search=&categorySlugs=food,toys&brandSlugs=kong&petTypes=dog
   * &priceMin=100000&priceMax=500000&inStockOnly=true &sort=price-asc&page=0&size=12
   */
  @GetMapping
  public ApiResp<ProductListResp> searchProducts(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) List<String> categorySlugs,
      @RequestParam(required = false) List<String> brandSlugs,
      @RequestParam(required = false) List<PetType> petTypes,
      @RequestParam(required = false) BigDecimal priceMin,
      @RequestParam(required = false) BigDecimal priceMax,
      @RequestParam(required = false) Boolean inStockOnly,
      @RequestParam(required = false, defaultValue = "featured") String sort,
      @RequestParam(required = false, defaultValue = "0") Integer page,
      @RequestParam(required = false, defaultValue = "12") Integer size) {
    ProductFilterRequest req =
        new ProductFilterRequest(
            search,
            categorySlugs,
            brandSlugs,
            petTypes,
            priceMin,
            priceMax,
            inStockOnly,
            sort,
            page,
            size);
    return ApiResp.<ProductListResp>builder()
        .message("Lấy danh sách sản phẩm thành công")
        .data(productService.searchProducts(req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/{slug}")
  public ApiResp<ProductResp> getProductBySlug(@PathVariable String slug) {
    return ApiResp.<ProductResp>builder()
        .message("Lấy chi tiết sản phẩm thành công")
        .data(productService.getProductBySlug(slug))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/{slug}/related")
  public ApiResp<List<ProductResp>> getRelatedProducts(
      @PathVariable String slug,
      @RequestParam(required = false, defaultValue = "4") Integer limit) {
    return ApiResp.<List<ProductResp>>builder()
        .message("Lấy sản phẩm tương tự thành công")
        .data(productService.getRelatedProducts(slug, limit))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/{slug}/reviews")
  public ApiResp<List<com.graduation.project.product.dto.resp.ProductReviewResp>> getProductReviews(@PathVariable String slug) {
    return ApiResp.<List<com.graduation.project.product.dto.resp.ProductReviewResp>>builder()
        .message("Lấy đánh giá sản phẩm thành công")
        .data(productService.getProductReviews(slug))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/featured")
  public ApiResp<List<ProductResp>> getFeaturedProducts(
      @RequestParam(required = false, defaultValue = "8") Integer limit) {
    return ApiResp.<List<ProductResp>>builder()
        .message("Lấy sản phẩm nổi bật thành công")
        .data(productService.getFeaturedProducts(limit))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ============ Quản trị (Staff) ============

  @PostMapping
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
  public ApiResp<ProductResp> createProduct(@Valid @RequestBody ProductReq req) {
    return ApiResp.<ProductResp>builder()
        .message("Thêm sản phẩm thành công")
        .data(productService.createProduct(req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
  public ApiResp<ProductResp> updateProduct(
      @PathVariable UUID id, @Valid @RequestBody ProductReq req) {
    return ApiResp.<ProductResp>builder()
        .message("Cập nhật sản phẩm thành công")
        .data(productService.updateProduct(id, req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
  public ApiResp<Void> deleteProduct(@PathVariable UUID id) {
    productService.deleteProduct(id);
    return ApiResp.<Void>builder()
        .message("Xóa sản phẩm thành công")
        .timestamp(Instant.now().toString())
        .build();
  }
}
