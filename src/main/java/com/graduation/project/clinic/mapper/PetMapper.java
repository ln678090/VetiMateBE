package com.graduation.project.clinic.mapper;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.PetRequest;
import com.graduation.project.clinic.entity.Pet;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PetMapper {

  @Mapping(target = "customerId", source = "customer.id")
  @Mapping(target = "customerName", source = "customer.fullName")
  PetDto toDto(Pet entity);

  // customer duoc gan trong service (tu customerId) -> ignore o day
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "currentHealthStatus", ignore = true)
  @Mapping(target = "currentHealthNote", ignore = true)
  @Mapping(target = "lastExaminedAt", ignore = true)
  Pet toEntity(PetRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "customer", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "deletedAt", ignore = true)
  @Mapping(target = "currentHealthStatus", ignore = true)
  @Mapping(target = "currentHealthNote", ignore = true)
  @Mapping(target = "lastExaminedAt", ignore = true)
  void updateEntity(PetRequest request, @MappingTarget Pet entity);
}
