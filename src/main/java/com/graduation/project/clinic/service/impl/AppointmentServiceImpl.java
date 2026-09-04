package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.AppointmentDto;
import com.graduation.project.clinic.dto.req.CreateAppointmentRequest;
import com.graduation.project.clinic.dto.req.UpdateAppointmentStatusRequest;
import com.graduation.project.clinic.dto.resp.AvailableSlotResponse;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
  private static final LocalTime OPEN_TIME = LocalTime.of(8, 0);
  private static final LocalTime CLOSE_TIME = LocalTime.of(17, 0);
  private final AppointmentRepository appointmentRepository;
  private final PetRepository petRepository;
  private final ClinicServiceRepository clinicServiceRepository;
  private final AppointmentMapper appointmentMapper;
  private static final LocalTime WORK_START = LocalTime.of(8, 0);
  private static final LocalTime WORK_END = LocalTime.of(17, 0);
  private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  @Override
  @Transactional(readOnly = true) // read-only -> Hibernate skip dirty checking, nhẹ hơn
  public List<AvailableSlotResponse> getAvailableSlots(UUID serviceId, LocalDate date) {

    // 1. Lấy duration của dịch vụ (fail-fast nếu không tồn tại)
    ClinicService service =
        clinicServiceRepository
            .findById(serviceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Service không tồn tại: " + serviceId));
    int durationMin = service.getDurationMin();

    // 2. Gom lịch đã đặt trong ngày (1 query)
    Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
    List<Appointment> booked =
        appointmentRepository.findActiveByServiceAndDay(serviceId, startOfDay, endOfDay);

    // 3. Sinh slot 8h -> 17h theo duration, loại overlap + slot quá khứ
    LocalDateTime now = LocalDateTime.now();
    List<AvailableSlotResponse> slots = new ArrayList<>();

    LocalTime cursor = OPEN_TIME;
    while (!cursor.plusMinutes(durationMin).isAfter(CLOSE_TIME)) {
      LocalTime slotEnd = cursor.plusMinutes(durationMin);
      LocalDateTime slotStartDt = LocalDateTime.of(date, cursor);
      LocalDateTime slotEndDt = LocalDateTime.of(date, slotEnd);

      boolean isPast = slotStartDt.isBefore(now);
      boolean overlaps =
          booked.stream()
              .anyMatch(
                  a ->
                      slotStartDt.isBefore(ChronoLocalDateTime.from(a.getEndAt()))
                          && slotEndDt.isAfter(ChronoLocalDateTime.from(a.getStartAt())));

      if (!isPast && !overlaps) {
        slots.add(new AvailableSlotResponse(cursor, slotEnd, true));
      }
      cursor = slotEnd; // slot liền kề, không chồng lấn
    }
    return slots;
  }

  @Override
  @Transactional
  public AppointmentDto create(CreateAppointmentRequest request) {
    // 1. Lấy pet + customer (JOIN FETCH) — BE tự suy customer từ pet, KHÔNG nhận từ
    // client
    Pet pet =
        petRepository
            .findByIdWithCustomer(request.petId())
            .orElseThrow(
                () -> new IllegalArgumentException("Không tìm thấy pet: " + request.petId()));

    // 2. Lấy service + kiểm tra đang active
    ClinicService service =
        clinicServiceRepository
            .findById(request.serviceId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Không tìm thấy dịch vụ: " + request.serviceId()));
    if (Boolean.FALSE.equals(service.getIsActive())) {
      throw new IllegalStateException("Dịch vụ đã ngừng hoạt động: " + service.getName());
    }

    // 3. Snapshot giá + thời lượng từ service (chống thay đổi giá về sau)
    Instant startAt = request.startAt();
    int durationMin = service.getDurationMin();
    Instant endAt = startAt.plus(durationMin, ChronoUnit.MINUTES);

    // 4. Chống trùng giờ (loại CANCELLED)
    if (appointmentRepository.existsOverlap(
        service.getId(), startAt, endAt, AppointmentStatus.CANCELLED)) {
      throw new IllegalStateException("Khung giờ này đã có lịch cho dịch vụ: " + service.getName());
    }

    // 5. Tạo appointment với dữ liệu BE tự tính
    Appointment appointment =
        Appointment.builder()
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
    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám: " + id));
    return appointmentMapper.toDto(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentDto> getByCustomer(UUID customerId, Pageable pageable) {
    return appointmentRepository
        .findByCustomerIdFull(customerId, pageable)
        .map(appointmentMapper::toDto);
  }

  @Override
  @Transactional
  public AppointmentDto updateStatus(UUID id, UpdateAppointmentStatusRequest request) {
    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám: " + id));
    appointment.setStatus(request.status());
    return appointmentMapper.toDto(appointmentRepository.save(appointment));
  }

  @Override
  @Transactional
  public AppointmentDto updateCallStatus(UUID id, boolean isCalled) {
    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch khám: " + id));
    appointment.setIsCalledToConfirm(isCalled);
    return appointmentMapper.toDto(appointmentRepository.save(appointment));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentDto> getForManagement(
      LocalDate startDate, LocalDate endDate, AppointmentStatus status, Pageable pageable) {

    LocalDate sDate = startDate != null ? startDate : LocalDate.now(ZONE);
    LocalDate eDate = endDate != null ? endDate : LocalDate.now(ZONE);

    Instant startAt = sDate.atStartOfDay(ZONE).toInstant();

    Instant endAt = eDate.plusDays(1).atStartOfDay(ZONE).toInstant();

    return appointmentRepository
        .findForManagement(startAt, endAt, status, pageable)
        .map(appointmentMapper::toDto);
  }
}
