package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.dto.req.CreateAppointmentRequest;
import com.graduation.project.clinic.dto.req.UpdateAppointmentStatusRequest;
import com.graduation.project.clinic.dto.resp.AvailableSlotResponse;
import com.graduation.project.clinic.entity.AppointmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
  AppointmentDto create(CreateAppointmentRequest request);

  AppointmentDto getById(UUID id);

  Page<AppointmentDto> getByCustomer(UUID customerId, Pageable pageable);

  AppointmentDto updateStatus(UUID id, UpdateAppointmentStatusRequest request);

  AppointmentDto updateCallStatus(UUID id, boolean isCalled);

  List<AvailableSlotResponse> getAvailableSlots(UUID serviceId, LocalDate date);

  Page<AppointmentDto> getForManagement(
      LocalDate startDate, LocalDate endDate, AppointmentStatus status, Pageable pageable);
}
