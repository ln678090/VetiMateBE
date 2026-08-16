package com.graduation.project.catalog.service;

import com.graduation.project.catalog.dto.req.CreateBrandRequest;
import com.graduation.project.catalog.dto.req.CreateCategoryRequest;
import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import java.util.List;
import java.util.UUID;

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

  // ===== Staff CRUD =====
  
  CategoryResp createCategory(CreateCategoryRequest request);
  CategoryResp updateCategory(UUID id, CreateCategoryRequest request);
  void deleteCategory(UUID id);

  BrandResp createBrand(CreateBrandRequest request);
  BrandResp updateBrand(UUID id, CreateBrandRequest request);
  void deleteBrand(UUID id);
}
