package com.graduation.project.product.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.product.dto.req.CreateProductRequest;
import com.graduation.project.product.dto.req.UpdateProductRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/products")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_STAFF', 'MANAGER')")
public class StaffProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<ApiResp<ProductListResp>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search) {

    ProductListResp resp = productService.getAllProductsForStaff(page, size, search);
    return ResponseEntity.ok(ApiResp.<ProductListResp>builder()
        .message("Lấy danh sách sản phẩm thành công")
        .data(resp)
        .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<ProductResp>> getProductById(@PathVariable UUID id) {
    ProductResp resp = productService.getProductById(id);
    return ResponseEntity.ok(ApiResp.<ProductResp>builder()
        .message("Lấy chi tiết sản phẩm thành công")
        .data(resp)
        .build());
  }

  @PostMapping
  public ResponseEntity<ApiResp<ProductResp>> createProduct(
      @Valid @RequestBody CreateProductRequest request) {
    ProductResp resp = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResp.<ProductResp>builder()
            .message("Tạo sản phẩm thành công")
            .data(resp)
            .build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResp<ProductResp>> updateProduct(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateProductRequest request) {
    ProductResp resp = productService.updateProduct(id, request);
    return ResponseEntity.ok(ApiResp.<ProductResp>builder()
        .message("Cập nhật sản phẩm thành công")
        .data(resp)
        .build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResp<Void>> deleteProduct(@PathVariable UUID id) {
    productService.deleteProduct(id);
    return ResponseEntity.ok(ApiResp.<Void>builder()
        .message("Xóa (ẩn) sản phẩm thành công")
        .build());
  }
}
