package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.dto.AvailableSlotDto;
import com.graduation.project.clinic.dto.req.CreateAppointmentRequest;
import com.graduation.project.clinic.dto.req.UpdateAppointmentStatusRequest;
import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.AppointmentStatus;
import com.graduation.project.clinic.entity.ClinicService;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.mapper.AppointmentMapper;
import com.graduation.project.clinic.repository.AppointmentRepository;
import com.graduation.project.clinic.repository.ClinicServiceRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.clinic.service.AppointmentService;
import com.graduation.project.common.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final PetRepository petRepository;
  private final ClinicServiceRepository clinicServiceRepository;
  private final AppointmentMapper appointmentMapper;
  private static final LocalTime WORK_START = LocalTime.of(8, 0);
  private static final LocalTime WORK_END = LocalTime.of(17, 0);
  private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  @Override
  @Transactional(readOnly = true)
  public List<AvailableSlotDto> getAvailableSlots(UUID serviceId, LocalDate date) {
    ClinicService service = clinicServiceRepository.findById(serviceId)
        .orElseThrow(() -> new UsernameNotFoundException("Dịch vụ không tồn tại"));

    int durationMin = service.getDurationMin();
    if (durationMin <= 0) {
      throw new IllegalStateException("Dịch vụ chưa cấu hình thời lượng hợp lệ");
    }

    // Khoảng ngày [00:00, 24:00) theo giờ VN -> Instant để query
    Instant dayStart = date.atStartOfDay(ZONE).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(ZONE).toInstant();

    // Lấy các lịch đã đặt trong ngày (trạng thái còn hiệu lực) - CHƯA có query này,
    // xem lưu ý
    List<Appointment> booked = appointmentRepository
        .findActiveBetween(dayStart, dayEnd);

    List<AvailableSlotDto> slots = new ArrayList<>();
    LocalDateTime cursor = LocalDateTime.of(date, WORK_START);
    LocalDateTime endOfDay = LocalDateTime.of(date, WORK_END);

    while (!cursor.plusMinutes(durationMin).isAfter(endOfDay)) {
      Instant slotStart = cursor.atZone(ZONE).toInstant();
      Instant slotEnd = cursor.plusMinutes(durationMin).atZone(ZONE).toInstant();

      boolean overlap = booked.stream()
          .anyMatch(a -> slotStart.isBefore(a.getEndAt()) && slotEnd.isAfter(a.getStartAt()));

      slots.add(new AvailableSlotDto(slotStart, slotEnd, !overlap));
      cursor = cursor.plusMinutes(durationMin);
    }
    return slots;
  }

  @Override
  @Transactional
  public AppointmentDto create(CreateAppointmentRequest request) {
    // 1. Lấy pet + customer (JOIN FETCH) — BE tự suy customer từ pet, KHÔNG nhận từ
    // client
    Pet pet = petRepository.findByIdWithCustomer(request.petId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pet: " + request.petId()));

    // 2. Lấy service + kiểm tra đang active
    ClinicService service = clinicServiceRepository.findById(request.serviceId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ: " + request.serviceId()));
    if (Boolean.FALSE.equals(service.getIsActive())) {
      throw new IllegalStateException("Dịch vụ đã ngừng hoạt động: " + service.getName());
    }

    // 3. Snapshot giá + thời lượng từ service (chống thay đổi giá về sau)
    Instant startAt = request.startAt();
    int durationMin = service.getDurationMin();
    Instant endAt = startAt.plus(durationMin, ChronoUnit.MINUTES);

    // 4. Chống trùng giờ (loại CANCELLED)
    if (appointmentRepository.existsOverlap(service.getId(), startAt, endAt, AppointmentStatus.CANCELLED)) {
      throw new IllegalStateException("Khung giờ này đã có lịch cho dịch vụ: " + service.getName());
    }

    // 5. Tạo appointment với dữ liệu BE tự tính
    Appointment appointment = Appointment.builder()
        .customer(pet.getCustomer())
        .pet(pet)
        .service(service)
        .priceSnapshot(service.getPrice())
        .durationMin(durationMin)
        .startAt(startAt)
        .endAt(endAt)
        .status(AppointmentStatus.SCHEDULED)
        .note(request.note())
        .build();

    return appointmentMapper.toDto(appointmentRepository.save(appointment));
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentDto getById(UUID id) {
    Appointment appointment = appointmentRepository.findByIdFull(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám: " + id));
    return appointmentMapper.toDto(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentDto> getByCustomer(UUID customerId, Pageable pageable) {
    return appointmentRepository.findByCustomerIdFull(customerId, pageable)
        .map(appointmentMapper::toDto);
  }

  @Override
  @Transactional
  public AppointmentDto updateStatus(UUID id, UpdateAppointmentStatusRequest request) {
    Appointment appointment = appointmentRepository.findByIdFull(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám: " + id));
    appointment.setStatus(request.status());
    return appointmentMapper.toDto(appointmentRepository.save(appointment));
  }
}
