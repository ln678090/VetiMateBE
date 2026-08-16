package com.graduation.project.catalog.service.Impl;

import com.graduation.project.catalog.dto.req.CreateBrandRequest;
import com.graduation.project.catalog.dto.req.CreateCategoryRequest;
import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import com.graduation.project.catalog.mapper.CatalogMapper;
import com.graduation.project.catalog.repository.BrandRepository;
import com.graduation.project.catalog.repository.CategoryRepository;
import com.graduation.project.catalog.service.CatalogService;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    List<Category> all = categoryRepository.findAllActive();

    Map<UUID, List<Category>> byParent =
        all.stream()
            .filter(c -> c.getParent() != null)
            .collect(Collectors.groupingBy(c -> c.getParent().getId()));

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

  // ===== Staff CRUD =====

  @Transactional
  @Override
  public CategoryResp createCategory(CreateCategoryRequest request) {
    String slug = toSlug(request.name());
    int counter = 1;
    String baseSlug = slug;
    while (categoryRepository.existsBySlug(slug)) {
      slug = baseSlug + "-" + counter++;
    }

    Category parent = null;
    if (request.parentId() != null) {
      parent = categoryRepository.findById(request.parentId())
          .orElseThrow(() -> new NoSuchElementException("Danh mục cha không tồn tại"));
    }

    Category category = Category.builder()
        .name(request.name())
        .slug(slug)
        .description(request.description())
        .icon(request.icon())
        .parent(parent)
        .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
        .isActive(true)
        .build();

    return catalogMapper.toCategoryResp(categoryRepository.save(category));
  }

  @Transactional
  @Override
  public CategoryResp updateCategory(UUID id, CreateCategoryRequest request) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));

    if (request.name() != null) {
      category.setName(request.name());
      String newSlug = toSlug(request.name());
      if (!newSlug.equals(category.getSlug())) {
        int counter = 1;
        String baseSlug = newSlug;
        while (categoryRepository.existsBySlug(newSlug) && !newSlug.equals(category.getSlug())) {
          newSlug = baseSlug + "-" + counter++;
        }
        category.setSlug(newSlug);
      }
    }

    if (request.description() != null) category.setDescription(request.description());
    if (request.icon() != null) category.setIcon(request.icon());
    if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
    
    if (request.parentId() != null) {
      Category parent = categoryRepository.findById(request.parentId())
          .orElseThrow(() -> new NoSuchElementException("Danh mục cha không tồn tại"));
      category.setParent(parent);
    } else {
      category.setParent(null);
    }

    return catalogMapper.toCategoryResp(categoryRepository.save(category));
  }

  @Transactional
  @Override
  public void deleteCategory(UUID id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));
    category.setIsActive(false);
    categoryRepository.save(category);
  }

  @Transactional
  @Override
  public BrandResp createBrand(CreateBrandRequest request) {
    String slug = toSlug(request.name());
    int counter = 1;
    String baseSlug = slug;
    while (brandRepository.existsBySlug(slug)) {
      slug = baseSlug + "-" + counter++;
    }

    Brand brand = Brand.builder()
        .name(request.name())
        .slug(slug)
        .description(request.description())
        .logoUrl(request.logoUrl())
        .isActive(true)
        .build();

    return catalogMapper.toBrandResp(brandRepository.save(brand));
  }

  @Transactional
  @Override
  public BrandResp updateBrand(UUID id, CreateBrandRequest request) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Thương hiệu không tồn tại"));

    if (request.name() != null) {
      brand.setName(request.name());
      String newSlug = toSlug(request.name());
      if (!newSlug.equals(brand.getSlug())) {
        int counter = 1;
        String baseSlug = newSlug;
        while (brandRepository.existsBySlug(newSlug) && !newSlug.equals(brand.getSlug())) {
          newSlug = baseSlug + "-" + counter++;
        }
        brand.setSlug(newSlug);
      }
    }

    if (request.description() != null) brand.setDescription(request.description());
    if (request.logoUrl() != null) brand.setLogoUrl(request.logoUrl());

    return catalogMapper.toBrandResp(brandRepository.save(brand));
  }

  @Transactional
  @Override
  public void deleteBrand(UUID id) {
    Brand brand = brandRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Thương hiệu không tồn tại"));
    brand.setIsActive(false);
    brandRepository.save(brand);
  }

  private String toSlug(String name) {
    String normalized = Normalizer.normalize(name, Normalizer.Form.NFD);
    return normalized
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("đ", "d").replaceAll("Đ", "D")
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("[\\s]+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }
}
