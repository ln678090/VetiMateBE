package com.graduation.project.catalog.mapper;

import com.graduation.project.catalog.dto.resp.BrandResp;
import com.graduation.project.catalog.dto.resp.CategoryResp;
import com.graduation.project.catalog.dto.resp.CategoryTreeResp;
import com.graduation.project.catalog.entity.Brand;
import com.graduation.project.catalog.entity.Category;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CatalogMapper {

  @Mapping(target = "parentId", source = "parent.id")
  CategoryResp toCategoryResp(Category category);

  BrandResp toBrandResp(Brand brand);

  List<BrandResp> toBrandRespList(List<Brand> brands);

  /**
   * Tree mapping — children sẽ được map đệ quy tự động bởi MapStruct. Lưu ý: chỉ truyền vào list đã
   * được lọc children theo parent.
   */
  @Mapping(target = "children", source = "children")
  CategoryTreeResp toCategoryTreeResp(Category category);
}
