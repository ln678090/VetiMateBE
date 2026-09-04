package com.graduation.project.catalog.service;

import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import java.util.List;

public interface CatalogService {

  /** Trả về cây phân cấp categories - dùng cho sidebar shop */
  List<CategoryTreeResp> getCategoryTree();

  /** Trả về flat list active categories */
  List<CategoryResp> getAllCategories();

  /** Lấy 1 category theo slug */
  CategoryResp getCategoryBySlug(String slug);

  /** Lấy toàn bộ brand active */
  List<BrandResp> getAllBrands();

  /** Lấy 1 brand theo slug */
  BrandResp getBrandBySlug(String slug);

  // ===== Quản lý =====
  CategoryResp createCategory(com.graduation.project.catalog.dto.req.CategoryReq req);

  CategoryResp updateCategory(
      java.util.UUID id, com.graduation.project.catalog.dto.req.CategoryReq req);

  void deleteCategory(java.util.UUID id);

  BrandResp createBrand(com.graduation.project.catalog.dto.req.BrandReq req);

  BrandResp updateBrand(java.util.UUID id, com.graduation.project.catalog.dto.req.BrandReq req);

  void deleteBrand(java.util.UUID id);
}
