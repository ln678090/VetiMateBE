package com.graduation.project.inventory.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.product.dto.req.UpdateProductRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_STAFF', 'MANAGER')")
public class StaffInventoryController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<ApiResp<ProductListResp>> getInventoryStock(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String search) {

    // Reusing the product list but focusing on stock management on the FE
    ProductListResp resp = productService.getAllProductsForStaff(page, size, search);
    return ResponseEntity.ok(ApiResp.<ProductListResp>builder()
        .message("Lấy tồn kho thành công")
        .data(resp)
        .build());
  }

  @PutMapping("/{productId}")
  public ResponseEntity<ApiResp<ProductResp>> updateStock(
      @PathVariable UUID productId,
      @Valid @RequestBody UpdateProductRequest request) {
    // This allows quick updates of stockQuantity
    ProductResp resp = productService.updateProduct(productId, request);
    return ResponseEntity.ok(ApiResp.<ProductResp>builder()
        .message("Cập nhật tồn kho thành công")
        .data(resp)
        .build());
  }
}
