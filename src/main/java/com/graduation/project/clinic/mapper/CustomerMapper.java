package com.graduation.project.clinic.mapper;

import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.dto.req.CustomerRequest;
import com.graduation.project.clinic.entity.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

  CustomerDto toDto(Customer entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "pets", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Customer toEntity(CustomerRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "pets", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(CustomerRequest request, @MappingTarget Customer entity);
}
