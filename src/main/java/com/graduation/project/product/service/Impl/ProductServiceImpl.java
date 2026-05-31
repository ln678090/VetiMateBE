package com.graduation.project.product.service.Impl;

import com.graduation.project.product.dto.req.ProductFilterRequest;
import com.graduation.project.product.dto.resp.ProductListResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.entity.Product.PetType;
import com.graduation.project.product.mapper.ProductMapper;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.product.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
