package com.graduation.project.catalog.controller;

import com.graduation.project.catalog.dto.req.CreateBrandRequest;
import com.graduation.project.catalog.dto.req.CreateCategoryRequest;
import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.service.CatalogService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/catalog")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_STAFF', 'MANAGER')")
public class StaffCatalogController {

  private final CatalogService catalogService;

  @PostMapping("/categories")
  public ResponseEntity<ApiResp<CategoryResp>> createCategory(
      @Valid @RequestBody CreateCategoryRequest request) {
    CategoryResp resp = catalogService.createCategory(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResp.<CategoryResp>builder()
            .message("Tạo danh mục thành công")
            .data(resp)
            .build());
  }

  @PutMapping("/categories/{id}")
  public ResponseEntity<ApiResp<CategoryResp>> updateCategory(
      @PathVariable UUID id,
      @Valid @RequestBody CreateCategoryRequest request) {
    CategoryResp resp = catalogService.updateCategory(id, request);
    return ResponseEntity.ok(ApiResp.<CategoryResp>builder()
        .message("Cập nhật danh mục thành công")
        .data(resp)
        .build());
  }

  @DeleteMapping("/categories/{id}")
  public ResponseEntity<ApiResp<Void>> deleteCategory(@PathVariable UUID id) {
    catalogService.deleteCategory(id);
    return ResponseEntity.ok(ApiResp.<Void>builder()
        .message("Xóa danh mục thành công")
        .build());
  }

  @PostMapping("/brands")
  public ResponseEntity<ApiResp<BrandResp>> createBrand(
      @Valid @RequestBody CreateBrandRequest request) {
    BrandResp resp = catalogService.createBrand(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResp.<BrandResp>builder()
            .message("Tạo thương hiệu thành công")
            .data(resp)
            .build());
  }

  @PutMapping("/brands/{id}")
  public ResponseEntity<ApiResp<BrandResp>> updateBrand(
      @PathVariable UUID id,
      @Valid @RequestBody CreateBrandRequest request) {
    BrandResp resp = catalogService.updateBrand(id, request);
    return ResponseEntity.ok(ApiResp.<BrandResp>builder()
        .message("Cập nhật thương hiệu thành công")
        .data(resp)
        .build());
  }

  @DeleteMapping("/brands/{id}")
  public ResponseEntity<ApiResp<Void>> deleteBrand(@PathVariable UUID id) {
    catalogService.deleteBrand(id);
    return ResponseEntity.ok(ApiResp.<Void>builder()
        .message("Xóa thương hiệu thành công")
        .build());
  }
}
