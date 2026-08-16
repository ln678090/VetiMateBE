package com.graduation.project.product.service.Impl;

import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import com.graduation.project.catalog.repository.BrandRepository;
import com.graduation.project.catalog.repository.CategoryRepository;
import com.graduation.project.product.dto.req.CreateProductRequest;
import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.req.UpdateProductRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.entity.Product.PetType;
import com.graduation.project.product.mapper.ProductMapper;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.product.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;
  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;

  @Override
  public ProductListResp searchProducts(ProductFilterRequest filter) {
    Specification<Product> spec = buildSpec(filter);
    Pageable pageable = PageRequest.of(filter.page(), filter.size(), buildSort(filter.sort()));

    Page<Product> result = productRepository.findAll(spec, pageable);

    return new ProductListResp(
        productMapper.toRespList(result.getContent()),
        result.getTotalElements(),
        result.getNumber(),
        result.getSize(),
        result.getTotalPages());
  }

  @Override
  public ProductResp getProductBySlug(String slug) {
    Product p =
        productRepository
            .findBySlugAndIsActiveTrue(slug)
            .orElseThrow(
                () -> new NoSuchElementException("Không tìm thấy sản phẩm với slug: " + slug));
    return productMapper.toResp(p);
  }

  @Override
  public List<ProductResp> getRelatedProducts(String currentSlug, int limit) {
    Product current =
        productRepository
            .findBySlugAndIsActiveTrue(currentSlug)
            .orElseThrow(
                () -> new NoSuchElementException("Không tìm thấy sản phẩm: " + currentSlug));

    Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 20)));
    return productMapper.toRespList(
        productRepository.findRelatedProducts(
            current.getCategory().getId(), currentSlug, pageable));
  }

  @Override
  public List<ProductResp> getFeaturedProducts(int limit) {
    Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 20)));
    return productMapper.toRespList(
        productRepository
            .findByIsFeaturedTrueAndIsActiveTrueOrderByRatingDesc(pageable)
            .getContent());
  }

  // ===== Staff CRUD =====

  @Override
  public ProductListResp getAllProductsForStaff(int page, int size, String search) {
    Specification<Product> spec = (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (search != null && !search.isBlank()) {
        String pattern = "%" + search.trim().toLowerCase() + "%";
        predicates.add(
            cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("sku")), pattern)));
      }
      return predicates.isEmpty() ? null : cb.and(predicates.toArray(new Predicate[0]));
    };

    Pageable pageable = PageRequest.of(
        Math.max(0, page),
        Math.max(1, Math.min(size, 100)),
        Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<Product> result = productRepository.findAll(spec, pageable);
    return new ProductListResp(
        productMapper.toRespList(result.getContent()),
        result.getTotalElements(),
        result.getNumber(),
        result.getSize(),
        result.getTotalPages());
  }

  @Override
  public ProductResp getProductById(UUID id) {
    Product p = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm: " + id));
    return productMapper.toResp(p);
  }

  @Transactional
  @Override
  public ProductResp createProduct(CreateProductRequest req) {
    Category category = categoryRepository.findById(req.categoryId())
        .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));
    Brand brand = brandRepository.findById(req.brandId())
        .orElseThrow(() -> new NoSuchElementException("Thương hiệu không tồn tại"));

    String slug = toSlug(req.name());
    // Ensure unique slug
    int counter = 1;
    String baseSlug = slug;
    while (productRepository.existsBySlug(slug)) {
      slug = baseSlug + "-" + counter++;
    }

    Product product = Product.builder()
        .name(req.name())
        .slug(slug)
        .sku(req.sku())
        .description(req.description())
        .shortDesc(req.shortDesc())
        .category(category)
        .brand(brand)
        .petType(req.petType())
        .price(req.price())
        .originalPrice(req.originalPrice())
        .stockQuantity(req.stockQuantity())
        .imageUrl(req.imageUrl())
        .galleryUrls(req.galleryUrls())
        .isFeatured(req.isFeatured() != null ? req.isFeatured() : false)
        .isNew(req.isNew() != null ? req.isNew() : false)
        .isActive(true)
        .build();

    return productMapper.toResp(productRepository.save(product));
  }

  @Transactional
  @Override
  public ProductResp updateProduct(UUID id, UpdateProductRequest req) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm: " + id));

    if (req.name() != null) {
      product.setName(req.name());
      // Regenerate slug if name changes
      String newSlug = toSlug(req.name());
      if (!newSlug.equals(product.getSlug())) {
        int counter = 1;
        String baseSlug = newSlug;
        while (productRepository.existsBySlug(newSlug) && !newSlug.equals(product.getSlug())) {
          newSlug = baseSlug + "-" + counter++;
        }
        product.setSlug(newSlug);
      }
    }
    if (req.sku() != null) product.setSku(req.sku());
    if (req.description() != null) product.setDescription(req.description());
    if (req.shortDesc() != null) product.setShortDesc(req.shortDesc());
    if (req.categoryId() != null) {
      Category category = categoryRepository.findById(req.categoryId())
          .orElseThrow(() -> new NoSuchElementException("Danh mục không tồn tại"));
      product.setCategory(category);
    }
    if (req.brandId() != null) {
      Brand brand = brandRepository.findById(req.brandId())
          .orElseThrow(() -> new NoSuchElementException("Thương hiệu không tồn tại"));
      product.setBrand(brand);
    }
    if (req.petType() != null) product.setPetType(req.petType());
    if (req.price() != null) product.setPrice(req.price());
    if (req.originalPrice() != null) product.setOriginalPrice(req.originalPrice());
    if (req.stockQuantity() != null) product.setStockQuantity(req.stockQuantity());
    if (req.imageUrl() != null) product.setImageUrl(req.imageUrl());
    if (req.galleryUrls() != null) product.setGalleryUrls(req.galleryUrls());
    if (req.isFeatured() != null) product.setIsFeatured(req.isFeatured());
    if (req.isNew() != null) product.setIsNew(req.isNew());
    if (req.isActive() != null) product.setIsActive(req.isActive());

    return productMapper.toResp(productRepository.save(product));
  }

  @Transactional
  @Override
  public void deleteProduct(UUID id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm: " + id));
    product.setIsActive(false);
    productRepository.save(product);
  }

  // ============ Helpers ============

  private String toSlug(String name) {
    String normalized = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD);
    return normalized
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("đ", "d").replaceAll("Đ", "D")
        .toLowerCase()
        .replaceAll("[^a-z0-9\\s-]", "")
        .replaceAll("[\\s]+", "-")
        .replaceAll("-+", "-")
        .replaceAll("^-|-$", "");
  }

  // ============ Specification builder ============

  private Specification<Product> buildSpec(ProductFilterRequest f) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Always active
      predicates.add(cb.isTrue(root.get("isActive")));

      // Search by name OR description
      if (f.search() != null && !f.search().isBlank()) {
        String pattern = "%" + f.search().trim().toLowerCase() + "%";
        predicates.add(
            cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern),
                cb.like(cb.lower(root.get("brand").get("name")), pattern)));
      }

      // Category slugs (in)
      if (f.categorySlugs() != null && !f.categorySlugs().isEmpty()) {
        predicates.add(root.get("category").get("slug").in(f.categorySlugs()));
      }

      // Brand slugs (in)
      if (f.brandSlugs() != null && !f.brandSlugs().isEmpty()) {
        predicates.add(root.get("brand").get("slug").in(f.brandSlugs()));
      }

      // Pet types - both luôn match
      if (f.petTypes() != null && !f.petTypes().isEmpty()) {
        List<PetType> typesToMatch = new ArrayList<>(f.petTypes());
        if (!typesToMatch.contains(PetType.both)) {
          typesToMatch.add(PetType.both);
        }
        predicates.add(root.get("petType").in(typesToMatch));
      }

      // Price range
      if (f.priceMin() != null) {
        predicates.add(cb.greaterThanOrEqualTo(root.get("price"), f.priceMin()));
      }
      if (f.priceMax() != null) {
        predicates.add(cb.lessThanOrEqualTo(root.get("price"), f.priceMax()));
      }

      // In stock only
      if (Boolean.TRUE.equals(f.inStockOnly())) {
        predicates.add(cb.greaterThan(root.get("stockQuantity"), 0));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private Sort buildSort(String sortKey) {
    return switch (sortKey == null ? "featured" : sortKey) {
      case "price-asc" -> Sort.by(Sort.Direction.ASC, "price");
      case "price-desc" -> Sort.by(Sort.Direction.DESC, "price");
      case "rating-desc" ->
          Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "reviewCount"));
      case "newest" ->
          Sort.by(Sort.Direction.DESC, "isNew").and(Sort.by(Sort.Direction.DESC, "createdAt"));
      default ->
          Sort.by(Sort.Direction.DESC, "isFeatured").and(Sort.by(Sort.Direction.DESC, "rating"));
    };
  }
}
