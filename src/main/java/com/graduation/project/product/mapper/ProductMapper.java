package com.graduation.project.product.mapper;

import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.product.entity.Product;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(target = "categoryId", source = "category.id")
  @Mapping(target = "categoryName", source = "category.name")
  @Mapping(target = "categorySlug", source = "category.slug")
  @Mapping(target = "brandId", source = "brand.id")
  @Mapping(target = "brandName", source = "brand.name")
  @Mapping(target = "brandSlug", source = "brand.slug")
  @Mapping(target = "inStock", expression = "java(product.isInStock())")
  ProductResp toResp(Product product);

  List<ProductResp> toRespList(List<Product> products);
}
