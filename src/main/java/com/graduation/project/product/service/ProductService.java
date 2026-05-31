package com.graduation.project.product.service;

import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import java.util.List;

public interface ProductService {

  /** Filter + paginate */
  ProductListResp searchProducts(ProductFilterRequest filter);

  /** Get 1 product by slug */
  ProductResp getProductBySlug(String slug);

  /** Get related products */
  List<ProductResp> getRelatedProducts(String currentSlug, int limit);

  /** Featured cho landing page */
  List<ProductResp> getFeaturedProducts(int limit);
}
