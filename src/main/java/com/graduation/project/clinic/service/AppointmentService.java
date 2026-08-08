package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.dto.req.CreateAppointmentRequest;
import com.graduation.project.clinic.dto.req.UpdateAppointmentStatusRequest;
import com.graduation.project.clinic.dto.resp.AvailableSlotResponse;
import com.graduation.project.clinic.entity.AppointmentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentService {
  AppointmentDto create(CreateAppointmentRequest request);

  AppointmentDto getById(UUID id);

  Page<AppointmentDto> getByCustomer(UUID customerId, Pageable pageable);

  AppointmentDto updateStatus(UUID id, UpdateAppointmentStatusRequest request);

  List<AvailableSlotResponse> getAvailableSlots(UUID serviceId, LocalDate date);

  Page<AppointmentDto> getForManagement(
      AppointmentStatus status,
      LocalDate date,
      Pageable pageable);

  Page<AppointmentDto> getForManagement(
      LocalDate date,
      AppointmentStatus status,
      Pageable pageable);
}
