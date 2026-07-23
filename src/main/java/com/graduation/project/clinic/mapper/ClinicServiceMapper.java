package com.graduation.project.clinic.mapper;

import com.graduation.project.clinic.dto.ClinicServiceDto;
import com.graduation.project.clinic.dto.req.ClinicServiceRequest;
import com.graduation.project.clinic.entity.ClinicService;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClinicServiceMapper {

  ClinicServiceDto toDto(ClinicService entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ClinicService toEntity(ClinicServiceRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(ClinicServiceRequest request, @MappingTarget ClinicService entity);
}
