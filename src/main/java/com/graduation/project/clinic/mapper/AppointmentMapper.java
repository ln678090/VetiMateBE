package com.graduation.project.clinic.mapper;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.entity.Appointment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppointmentMapper {

  @Mapping(target = "customerId", source = "customer.id")
  @Mapping(target = "customerName", source = "customer.fullName")
  @Mapping(target = "petId", source = "pet.id")
  @Mapping(target = "petName", source = "pet.name")
  @Mapping(target = "serviceId", source = "service.id")
  @Mapping(target = "serviceName", source = "service.name")
  AppointmentDto toDto(Appointment entity);
}
