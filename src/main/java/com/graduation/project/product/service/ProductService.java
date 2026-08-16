package com.graduation.project.product.service;

import com.graduation.project.product.dto.req.CreateProductRequest;
import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.req.UpdateProductRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import java.util.List;
import java.util.UUID;

public interface ProductService {

  /** Filter + paginate */
  ProductListResp searchProducts(ProductFilterRequest filter);

  /** Get 1 product by slug */
  ProductResp getProductBySlug(String slug);

  /** Get related products */
  List<ProductResp> getRelatedProducts(String currentSlug, int limit);

  /** Featured cho landing page */
  List<ProductResp> getFeaturedProducts(int limit);

  // ===== Staff CRUD =====

  /** Staff: lấy tất cả SP (kể cả inactive) */
  ProductListResp getAllProductsForStaff(int page, int size, String search);

  /** Staff: lấy SP theo ID */
  ProductResp getProductById(UUID id);

  /** Staff: tạo sản phẩm mới */
  ProductResp createProduct(CreateProductRequest request);

  /** Staff: cập nhật sản phẩm */
  ProductResp updateProduct(UUID id, UpdateProductRequest request);

  /** Staff: soft delete sản phẩm */
  void deleteProduct(UUID id);
}
