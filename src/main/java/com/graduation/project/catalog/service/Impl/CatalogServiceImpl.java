package com.graduation.project.catalog.service.Impl;

import com.graduation.project.catalog.dto.req.BrandReq;
import com.graduation.project.catalog.dto.req.CategoryReq;
import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import com.graduation.project.catalog.mapper.CatalogMapper;
import com.graduation.project.catalog.repository.BrandRepository;
import com.graduation.project.catalog.repository.CategoryRepository;
import com.graduation.project.catalog.service.CatalogService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import java.text.Normalizer;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;
  private final CatalogMapper catalogMapper;

  @Override
  public List<CategoryTreeResp> getCategoryTree() {
    // 1 query duy nhất → build tree in-memory (tránh N+1)
    List<Category> all = categoryRepository.findAllActive();

    // Group theo parent_id
    Map<UUID, List<Category>> byParent =
        all.stream()
            .filter(c -> c.getParent() != null)
            .collect(Collectors.groupingBy(c -> c.getParent().getId()));

    // Lấy roots
    return all.stream()
        .filter(c -> c.getParent() == null)
        .sorted(Comparator.comparing(Category::getSortOrder).thenComparing(Category::getName))
        .map(root -> buildTreeNode(root, byParent))
        .toList();
  }

  private CategoryTreeResp buildTreeNode(Category cat, Map<UUID, List<Category>> byParent) {
    List<CategoryTreeResp> childTrees =
        byParent.getOrDefault(cat.getId(), new ArrayList<>()).stream()
            .sorted(Comparator.comparing(Category::getSortOrder).thenComparing(Category::getName))
            .map(child -> buildTreeNode(child, byParent))
            .toList();

    return new CategoryTreeResp(
        cat.getId(),
        cat.getName(),
        cat.getSlug(),
        cat.getDescription(),
        cat.getIcon(),
        cat.getSortOrder(),
        childTrees);
  }

  @Override
  public List<CategoryResp> getAllCategories() {
    return categoryRepository.findAllActive().stream().map(catalogMapper::toCategoryResp).toList();
  }

  @Override
  public CategoryResp getCategoryBySlug(String slug) {
    Category category =
        categoryRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> new NoSuchElementException("Không tìm thấy danh mục với slug:" + slug));
    return catalogMapper.toCategoryResp(category);
  }

  @Override
  public List<BrandResp> getAllBrands() {
    return catalogMapper.toBrandRespList(brandRepository.findAllActive());
  }

  @Override
  public BrandResp getBrandBySlug(String slug) {
    Brand brand =
        brandRepository
            .findBySlug(slug)
            .orElseThrow(
                () -> new NoSuchElementException("Không tìm thấy thương hiệu với slug: " + slug));
    return catalogMapper.toBrandResp(brand);
  }

  // ===== Utils =====
  private String generateSlug(String input) {
    if (input == null) return "";
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    String slug = pattern.matcher(normalized).replaceAll("").toLowerCase();
    return slug.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
  }

  // ===== Quản lý Danh mục =====
  
  @Override
  @Transactional
  public CategoryResp createCategory(CategoryReq req) {
    Category category = new Category();
    category.setName(req.getName());
    category.setSlug(generateSlug(req.getName()) + "-" + System.currentTimeMillis());
    category.setDescription(req.getDescription());
    category.setIsActive(req.getIsActive());
    
    if (req.getParentId() != null) {
      Category parent = categoryRepository.findById(req.getParentId())
          .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục cha"));
      category.setParent(parent);
    }
    
    category = categoryRepository.save(category);
    return catalogMapper.toCategoryResp(category);
  }

  @Override
  @Transactional
  public CategoryResp updateCategory(UUID id, CategoryReq req) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục"));
        
    category.setName(req.getName());
    // Only update slug if you want, usually slugs are immutable or regenerated. We keep it simple here.
    category.setDescription(req.getDescription());
    category.setIsActive(req.getIsActive());
    
    if (req.getParentId() != null) {
      if (req.getParentId().equals(id)) {
         throw new IllegalArgumentException("Danh mục cha không thể là chính nó");
      }
      Category parent = categoryRepository.findById(req.getParentId())
          .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục cha"));
      category.setParent(parent);
    } else {
      category.setParent(null);
    }
    
    category = categoryRepository.save(category);
    return catalogMapper.toCategoryResp(category);
  }

  @Override
  @Transactional
  public void deleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục"));
        
    if (!category.getChildren().isEmpty()) {
       throw new IllegalStateException("Không thể xóa danh mục đang có danh mục con");
    }
    
    categoryRepository.delete(category);
  }

  // ===== Quản lý Thương hiệu =====

  @Override
  @Transactional
  public BrandResp createBrand(BrandReq req) {
    Brand brand = new Brand();
    brand.setName(req.getName());
    brand.setSlug(generateSlug(req.getName()) + "-" + System.currentTimeMillis());
    brand.setDescription(req.getDescription());
    brand.setLogoUrl(req.getLogoUrl());
    brand.setIsActive(req.getIsActive());
    
    brand = brandRepository.save(brand);
    return catalogMapper.toBrandResp(brand);
  }

  @Override
  @Transactional
  public BrandResp updateBrand(UUID id, BrandReq req) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thương hiệu"));
        
    brand.setName(req.getName());
    brand.setDescription(req.getDescription());
    brand.setLogoUrl(req.getLogoUrl());
    brand.setIsActive(req.getIsActive());
    
    brand = brandRepository.save(brand);
    return catalogMapper.toBrandResp(brand);
  }

  @Override
  @Transactional
  public void deleteBrand(UUID id) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thương hiệu"));
    brandRepository.delete(brand);
  }
}
