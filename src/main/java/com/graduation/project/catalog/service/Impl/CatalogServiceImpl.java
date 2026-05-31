package com.graduation.project.catalog.service.Impl;

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
}
