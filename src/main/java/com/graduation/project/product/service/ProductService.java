package com.graduation.project.product.service;

import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.req.ProductReq;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.dto.resp.ProductReviewResp;
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

  /** Get product reviews */
  List<ProductReviewResp> getProductReviews(String slug);

  // ===== Quản trị (Staff) =====
  ProductResp createProduct(ProductReq req);

  ProductResp updateProduct(UUID id, ProductReq req);

  void deleteProduct(UUID id);
}
