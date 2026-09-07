package com.graduation.project.clinic.mapper;

import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "fullName", source = "user.fullName")
  @Mapping(target = "phone", source = "user.phone")
  @Mapping(target = "email", source = "user.email")
  @Mapping(target = "address", source = "user.address")
  CustomerDto toDto(Customer customer);
}
