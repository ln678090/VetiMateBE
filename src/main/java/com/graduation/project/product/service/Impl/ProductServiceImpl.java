package com.graduation.project.product.service.Impl;

import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import com.graduation.project.catalog.repository.BrandRepository;
import com.graduation.project.catalog.repository.CategoryRepository;
import com.graduation.project.product.dto.req.ProductReq;
import com.graduation.project.product.dto.req.ProductFilterRequest;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
  private final CategoryRepository categoryRepository;
  private final BrandRepository brandRepository;
  private final com.graduation.project.clinic.repository.InvoiceReviewRepository invoiceReviewRepository;
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

  // ============ Quản trị (Staff) ============

  @Override
  @Transactional
  public ProductResp createProduct(ProductReq req) {
    Category category = categoryRepository.findById(req.getCategoryId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
    
    Brand brand = brandRepository.findById(req.getBrandId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu"));

    String slug = generateSlug(req.getName());
    if (productRepository.findBySlugAndIsActiveTrue(slug).isPresent()) {
      slug = slug + "-" + System.currentTimeMillis();
    }

    Product product = Product.builder()
        .name(req.getName())
        .slug(slug)
        .description(req.getDescription())
        .shortDesc(req.getShortDesc())
        .category(category)
        .brand(brand)
        .petType(req.getPetType())
        .price(req.getPrice())
        .originalPrice(req.getOriginalPrice())
        .stockQuantity(req.getStockQuantity())
        .imageUrl(req.getImageUrl())
        .galleryUrls(req.getGalleryUrls())
        .isFeatured(req.getIsFeatured())
        .isNew(req.getIsNew())
        .isActive(req.getIsActive())
        .build();

    product = productRepository.save(product);
    return productMapper.toResp(product);
  }

  @Override
  @Transactional
  public ProductResp updateProduct(UUID id, ProductReq req) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));

    Category category = categoryRepository.findById(req.getCategoryId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục"));
    
    Brand brand = brandRepository.findById(req.getBrandId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu"));

    if (!product.getName().equals(req.getName())) {
      String slug = generateSlug(req.getName());
      if (productRepository.findBySlugAndIsActiveTrue(slug).isPresent() && !product.getSlug().equals(slug)) {
        slug = slug + "-" + System.currentTimeMillis();
      }
      product.setSlug(slug);
    }

    product.setName(req.getName());
    product.setDescription(req.getDescription());
    product.setShortDesc(req.getShortDesc());
    product.setCategory(category);
    product.setBrand(brand);
    product.setPetType(req.getPetType());
    product.setPrice(req.getPrice());
    product.setOriginalPrice(req.getOriginalPrice());
    product.setStockQuantity(req.getStockQuantity());
    product.setImageUrl(req.getImageUrl());
    product.setGalleryUrls(req.getGalleryUrls());
    product.setIsFeatured(req.getIsFeatured());
    product.setIsNew(req.getIsNew());
    product.setIsActive(req.getIsActive());

    product = productRepository.save(product);
    return productMapper.toResp(product);
  }

  @Override
  @Transactional
  public void deleteProduct(UUID id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));
    product.setIsActive(false); // Soft delete
    productRepository.save(product);
  }

  @Override
  @Transactional(readOnly = true)
  public List<com.graduation.project.product.dto.resp.ProductReviewResp> getProductReviews(String slug) {
      List<com.graduation.project.clinic.entity.InvoiceReview> reviews = invoiceReviewRepository.findByProduct_SlugOrderByCreatedAtDesc(slug);
      
      return reviews.stream().map(review -> {
          String userName = review.getCustomer() != null ? review.getCustomer().getFullName() : "Khách hàng";
          String avatarStr = "";
          if (userName != null && !userName.isEmpty()) {
              String[] parts = userName.trim().split(" ");
              if (parts.length > 0) {
                  avatarStr = parts[parts.length - 1].substring(0, 1).toUpperCase();
                  if (parts.length > 1) {
                      avatarStr = parts[0].substring(0, 1).toUpperCase() + avatarStr;
                  }
              }
          }
          if (avatarStr.isEmpty()) avatarStr = "KH";
          
          return com.graduation.project.product.dto.resp.ProductReviewResp.builder()
              .id(review.getId())
              .user(userName)
              .avatar(avatarStr)
              .rating(review.getRating())
              .createdAt(review.getCreatedAt())
              .title(getReviewTitle(review.getRating()))
              .content(review.getComment())
              .helpful(0)
              .build();
      }).collect(Collectors.toList());
  }

  private String getReviewTitle(Integer rating) {
      if (rating == null) return "Tuyệt vời";
      return switch (rating) {
          case 1 -> "Rất không hài lòng";
          case 2 -> "Không hài lòng";
          case 3 -> "Bình thường";
          case 4 -> "Hài lòng";
          case 5 -> "Tuyệt vời";
          default -> "Tuyệt vời";
      };
  }

  private String generateSlug(String input) {
    if (input == null || input.isEmpty()) return "";
    String nowhitespace = Pattern.compile("[\\s]").matcher(input).replaceAll("-");
    String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
    String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll("");
    return slug.toLowerCase().replaceAll("-+", "-").replaceAll("^-|-$", "");
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
