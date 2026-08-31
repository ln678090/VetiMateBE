package com.graduation.project.catalog.controller;

import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import com.graduation.project.catalog.service.CatalogService;
import com.graduation.project.common.resp.ApiResp;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService catalogService;

  // ===== Categories =====

  @GetMapping("/categories/tree")
  public ApiResp<List<CategoryTreeResp>> getCategoryTree() {
    return ApiResp.<List<CategoryTreeResp>>builder()
        .message("Lấy cây danh mục thành công")
        .data(catalogService.getCategoryTree())
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/categories")
  public ApiResp<List<CategoryResp>> getAllCategories() {
    return ApiResp.<List<CategoryResp>>builder()
        .message("Lấy danh sách danh mục thành công")
        .data(catalogService.getAllCategories())
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/categories/{slug}")
  public ApiResp<CategoryResp> getCategoryBySlug(@PathVariable String slug) {
    return ApiResp.<CategoryResp>builder()
        .message("Lấy danh mục thành công")
        .data(catalogService.getCategoryBySlug(slug))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Brands =====

  @GetMapping("/brands")
  public ApiResp<List<BrandResp>> getAllBrands() {
    return ApiResp.<List<BrandResp>>builder()
        .message("Lấy danh sách thương hiệu thành công")
        .data(catalogService.getAllBrands())
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/brands/{slug}")
  public ApiResp<BrandResp> getBrandBySlug(@PathVariable String slug) {
    return ApiResp.<BrandResp>builder()
        .message("Lấy thương hiệu thành công")
        .data(catalogService.getBrandBySlug(slug))
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Quản lý Danh mục (Staff/Admin) =====
  
  @PostMapping("/categories")
  public ApiResp<CategoryResp> createCategory(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.graduation.project.catalog.dto.req.CategoryReq req) {
    return ApiResp.<CategoryResp>builder()
        .message("Tạo danh mục thành công")
        .data(catalogService.createCategory(req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/categories/{id}")
  public ApiResp<CategoryResp> updateCategory(
      @PathVariable java.util.UUID id, 
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.graduation.project.catalog.dto.req.CategoryReq req) {
    return ApiResp.<CategoryResp>builder()
        .message("Cập nhật danh mục thành công")
        .data(catalogService.updateCategory(id, req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/categories/{id}")
  public ApiResp<Void> deleteCategory(@PathVariable java.util.UUID id) {
    catalogService.deleteCategory(id);
    return ApiResp.<Void>builder()
        .message("Xóa danh mục thành công")
        .timestamp(Instant.now().toString())
        .build();
  }

  // ===== Quản lý Thương hiệu (Staff/Admin) =====

  @PostMapping("/brands")
  public ApiResp<BrandResp> createBrand(@jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.graduation.project.catalog.dto.req.BrandReq req) {
    return ApiResp.<BrandResp>builder()
        .message("Tạo thương hiệu thành công")
        .data(catalogService.createBrand(req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/brands/{id}")
  public ApiResp<BrandResp> updateBrand(
      @PathVariable java.util.UUID id, 
      @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.graduation.project.catalog.dto.req.BrandReq req) {
    return ApiResp.<BrandResp>builder()
        .message("Cập nhật thương hiệu thành công")
        .data(catalogService.updateBrand(id, req))
        .timestamp(Instant.now().toString())
        .build();
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/brands/{id}")
  public ApiResp<Void> deleteBrand(@PathVariable java.util.UUID id) {
    catalogService.deleteBrand(id);
    return ApiResp.<Void>builder()
        .message("Xóa thương hiệu thành công")
        .timestamp(Instant.now().toString())
        .build();
  }
}
