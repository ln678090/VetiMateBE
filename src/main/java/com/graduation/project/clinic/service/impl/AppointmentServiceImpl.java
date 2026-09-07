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
import com.graduation.project.notification.service.NotificationService;
import com.graduation.project.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

  private static final LocalTime WORK_START = LocalTime.of(8, 0);

  private static final LocalTime WORK_END = LocalTime.of(17, 0);

  private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private static final int MAX_RECENT_RECEPTIONISTS = 100;

  private static final String RECEPTIONIST_ROLE = "ROLE_RECEPTIONIST";

  private static final String OVERLAP_CONSTRAINT = "ex_appointments_service_time_overlap";

  private final AppointmentRepository appointmentRepository;
  private final PetRepository petRepository;
  private final ClinicServiceRepository clinicServiceRepository;
  private final AppointmentMapper appointmentMapper;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public List<AvailableSlotResponse> getAvailableSlots(UUID serviceId, LocalDate date) {
    requireId(serviceId, "Service ID");
    requireDate(date);

    ClinicService service =
        clinicServiceRepository
            .findById(serviceId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy dịch vụ: " + serviceId));

    if (Boolean.FALSE.equals(service.getIsActive())) {
      return List.of();
    }

    int durationMin = requireValidDuration(service.getDurationMin());

    Instant startOfDay = date.atStartOfDay(ZONE).toInstant();

    Instant endOfDay = date.plusDays(1).atStartOfDay(ZONE).toInstant();

    List<Appointment> bookedAppointments =
        appointmentRepository.findActiveByServiceAndDay(serviceId, startOfDay, endOfDay);

    Instant now = Instant.now();

    List<AvailableSlotResponse> availableSlots = new ArrayList<>();

    LocalTime cursor = WORK_START;

    while (!cursor.plusMinutes(durationMin).isAfter(WORK_END)) {

      LocalTime slotEndTime = cursor.plusMinutes(durationMin);

      Instant slotStart = LocalDateTime.of(date, cursor).atZone(ZONE).toInstant();

      Instant slotEnd = LocalDateTime.of(date, slotEndTime).atZone(ZONE).toInstant();

      boolean isPast = !slotStart.isAfter(now);

      boolean overlaps =
          bookedAppointments.stream()
              .anyMatch(
                  appointment ->
                      overlaps(
                          slotStart, slotEnd, appointment.getStartAt(), appointment.getEndAt()));

      if (!isPast && !overlaps) {
        availableSlots.add(new AvailableSlotResponse(cursor, slotEndTime, true));
      }

      cursor = slotEndTime;
    }

    return availableSlots;
  }

  @Override
  @Transactional
  public AppointmentDto create(CreateAppointmentRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("Thông tin đặt lịch là bắt buộc");
    }

    Pet pet =
        petRepository
            .findByIdWithCustomer(request.petId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy thú cưng: " + request.petId()));

    ClinicService service =
        clinicServiceRepository
            .findById(request.serviceId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Không tìm thấy dịch vụ: " + request.serviceId()));

    if (Boolean.FALSE.equals(service.getIsActive())) {
      throw new IllegalStateException("Dịch vụ đã ngừng hoạt động: " + service.getName());
    }

    Instant startAt = request.startAt();

    if (startAt == null) {
      throw new IllegalArgumentException("Thời gian bắt đầu là bắt buộc");
    }

    if (!startAt.isAfter(Instant.now())) {
      throw new IllegalArgumentException("Không thể đặt lịch trong quá khứ");
    }

    int durationMin = requireValidDuration(service.getDurationMin());

    Instant endAt = startAt.plus(durationMin, ChronoUnit.MINUTES);

    validateWorkingHours(startAt, endAt);

    /*
     * Kiểm tra nhanh để trả thông báo dễ hiểu.
     *
     * Đây không phải lớp bảo vệ cuối cùng vì hai request
     * đồng thời vẫn có thể cùng vượt qua câu kiểm tra này.
     */
    if (appointmentRepository.existsOverlap(
        service.getId(), startAt, endAt, AppointmentStatus.CANCELLED)) {
      throw new IllegalStateException(
          "Khung giờ này đã có người đặt. " + "Vui lòng chọn khung giờ khác.");
    }

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
            .isCalledToConfirm(false)
            .note(normalizeOptional(request.note()))
            .build();

    Appointment savedAppointment;

    try {
      /*
       * saveAndFlush buộc PostgreSQL kiểm tra exclusion
       * constraint ngay trong method này.
       */
      savedAppointment = appointmentRepository.saveAndFlush(appointment);
    } catch (DataIntegrityViolationException exception) {
      if (isAppointmentOverlapViolation(exception)) {
        throw new IllegalStateException(
            "Khung giờ này vừa được người khác đặt. " + "Vui lòng tải lại và chọn giờ khác.",
            exception);
      }

      throw exception;
    }

    /*
     * Notification được lưu trong cùng transaction.
     * Nếu transaction đặt lịch rollback thì notification
     * cũng không tồn tại.
     */
    notifyAppointmentOwner(savedAppointment);
    notifyReceptionists(savedAppointment);

    return appointmentMapper.toDto(savedAppointment);
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentDto getById(UUID id) {
    requireId(id, "Appointment ID");

    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám: " + id));

    return appointmentMapper.toDto(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentDto> getByCustomer(UUID customerId, Pageable pageable) {
    requireId(customerId, "Customer ID");

    if (pageable == null) {
      throw new IllegalArgumentException("Thông tin phân trang là bắt buộc");
    }

    return appointmentRepository
        .findByCustomerIdFull(customerId, pageable)
        .map(appointmentMapper::toDto);
  }

  @Override
  @Transactional
  public AppointmentDto updateStatus(UUID id, UpdateAppointmentStatusRequest request) {
    requireId(id, "Appointment ID");

    if (request == null || request.status() == null) {
      throw new IllegalArgumentException("Trạng thái lịch hẹn là bắt buộc");
    }

    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám: " + id));

    AppointmentStatus currentStatus = appointment.getStatus();

    AppointmentStatus targetStatus = request.status();

    if (currentStatus == targetStatus) {
      return appointmentMapper.toDto(appointment);
    }

    /*
     * DONE chỉ được đặt bởi ExaminationService.complete().
     * Không cho màn hình quản lý bỏ qua quy trình khám.
     */
    if (targetStatus == AppointmentStatus.DONE) {
      throw new IllegalStateException(
          "Không thể chuyển trực tiếp sang DONE. "
              + "Ca khám phải được hoàn tất "
              + "từ quy trình khám bệnh.");
    }

    validateStatusTransition(currentStatus, targetStatus);

    appointment.setStatus(targetStatus);

    Appointment savedAppointment = appointmentRepository.save(appointment);

    notifyOwnerAboutStatus(savedAppointment);

    return appointmentMapper.toDto(savedAppointment);
  }

  @Override
  @Transactional
  public AppointmentDto updateCallStatus(UUID id, boolean isCalled) {
    requireId(id, "Appointment ID");

    Appointment appointment =
        appointmentRepository
            .findByIdFull(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch khám: " + id));

    if (isTerminalStatus(appointment.getStatus())) {
      throw new IllegalStateException(
          "Không thể cập nhật trạng thái gọi " + "cho lịch đã kết thúc");
    }

    appointment.setIsCalledToConfirm(isCalled);

    return appointmentMapper.toDto(appointmentRepository.save(appointment));
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentDto> getForManagement(
      LocalDate startDate, LocalDate endDate, AppointmentStatus status, Pageable pageable) {
    if (pageable == null) {
      throw new IllegalArgumentException("Thông tin phân trang là bắt buộc");
    }

    LocalDate resolvedStartDate = startDate != null ? startDate : LocalDate.now(ZONE);

    LocalDate resolvedEndDate = endDate != null ? endDate : resolvedStartDate;

    if (resolvedEndDate.isBefore(resolvedStartDate)) {
      throw new IllegalArgumentException("Ngày kết thúc không được trước ngày bắt đầu");
    }

    Instant startAt = resolvedStartDate.atStartOfDay(ZONE).toInstant();

    Instant endAt = resolvedEndDate.plusDays(1).atStartOfDay(ZONE).toInstant();

    return appointmentRepository
        .findForManagement(startAt, endAt, status, pageable)
        .map(appointmentMapper::toDto);
  }

  private void validateWorkingHours(Instant startAt, Instant endAt) {
    ZonedDateTime localStart = startAt.atZone(ZONE);

    ZonedDateTime localEnd = endAt.atZone(ZONE);

    if (!localStart.toLocalDate().equals(localEnd.toLocalDate())) {
      throw new IllegalArgumentException(
          "Lịch hẹn phải bắt đầu và kết thúc " + "trong cùng một ngày");
    }

    LocalTime startTime = localStart.toLocalTime();

    LocalTime endTime = localEnd.toLocalTime();

    if (startTime.isBefore(WORK_START) || endTime.isAfter(WORK_END)) {
      throw new IllegalArgumentException(
          "Lịch hẹn phải nằm trong giờ làm việc " + "từ 08:00 đến 17:00");
    }
  }

  private void validateStatusTransition(
      AppointmentStatus currentStatus, AppointmentStatus targetStatus) {
    boolean validTransition =
        switch (currentStatus) {
          case SCHEDULED ->
              targetStatus == AppointmentStatus.CONFIRMED
                  || targetStatus == AppointmentStatus.CANCELLED;

          case CONFIRMED ->
              targetStatus == AppointmentStatus.CANCELLED
                  || targetStatus == AppointmentStatus.NO_SHOW;

          case DONE, CANCELLED, NO_SHOW -> false;

          default -> false;
        };

    if (!validTransition) {
      throw new IllegalStateException(
          "Không thể chuyển lịch hẹn từ " + currentStatus + " sang " + targetStatus);
    }
  }

  private void notifyAppointmentOwner(Appointment appointment) {
    UUID ownerUserId = appointment.getCustomer().getUser().getId();

    String message =
        "Lịch "
            + appointment.getService().getName()
            + " cho "
            + appointment.getPet().getName()
            + " đã được ghi nhận.";

    notificationService.createNotification(
        ownerUserId, "Đặt lịch thành công", message, ownerAppointmentLink(appointment.getId()));
  }

  private void notifyReceptionists(Appointment appointment) {
    List<UUID> receptionistIds = userRepository.findIdsByRoleName(RECEPTIONIST_ROLE);

    /*
     * Giới hạn phòng trường hợp dữ liệu role bị gán sai
     * hàng loạt, tránh tạo notification không kiểm soát.
     */
    receptionistIds.stream()
        .limit(MAX_RECENT_RECEPTIONISTS)
        .forEach(
            receptionistId ->
                notificationService.createNotification(
                    receptionistId,
                    "Có lịch hẹn mới",
                    buildReceptionistMessage(appointment),
                    managementAppointmentLink(appointment.getId())));
  }

  private void notifyOwnerAboutStatus(Appointment appointment) {
    String title;
    String message;

    switch (appointment.getStatus()) {
      case CONFIRMED -> {
        title = "Lịch hẹn đã được xác nhận";
        message =
            "Phòng khám đã xác nhận lịch "
                + appointment.getService().getName()
                + " cho "
                + appointment.getPet().getName()
                + ".";
      }

      case CANCELLED -> {
        title = "Lịch hẹn đã bị hủy";
        message =
            "Lịch "
                + appointment.getService().getName()
                + " của "
                + appointment.getPet().getName()
                + " đã bị hủy.";
      }

      case NO_SHOW -> {
        title = "Lịch hẹn đã quá giờ";
        message = "Lịch của " + appointment.getPet().getName() + " được ghi nhận là không đến.";
      }

      default -> {
        return;
      }
    }

    notificationService.createNotification(
        appointment.getCustomer().getUser().getId(),
        title,
        message,
        ownerAppointmentLink(appointment.getId()));
  }

  private String buildReceptionistMessage(Appointment appointment) {
    return "Có lịch "
        + appointment.getService().getName()
        + " mới cho "
        + appointment.getPet().getName()
        + ".";
  }

  private String ownerAppointmentLink(UUID appointmentId) {
    return "/booking?appointmentId=" + appointmentId;
  }

  private String managementAppointmentLink(UUID appointmentId) {
    return "/management/appointments?appointmentId=" + appointmentId;
  }

  private boolean overlaps(
      Instant firstStart, Instant firstEnd, Instant secondStart, Instant secondEnd) {
    return firstStart.isBefore(secondEnd) && firstEnd.isAfter(secondStart);
  }

  private boolean isAppointmentOverlapViolation(Throwable throwable) {
    Throwable current = throwable;

    while (current != null) {
      String message = current.getMessage();

      if (message != null && message.contains(OVERLAP_CONSTRAINT)) {
        return true;
      }

      current = current.getCause();
    }

    return false;
  }

  private boolean isTerminalStatus(AppointmentStatus status) {
    return status == AppointmentStatus.DONE
        || status == AppointmentStatus.CANCELLED
        || status == AppointmentStatus.NO_SHOW;
  }

  private int requireValidDuration(Integer durationMin) {
    if (durationMin == null || durationMin <= 0) {
      throw new IllegalStateException("Thời lượng dịch vụ không hợp lệ");
    }

    return durationMin;
  }

  private void requireId(UUID id, String fieldName) {
    if (id == null) {
      throw new IllegalArgumentException(fieldName + " là bắt buộc");
    }
  }

  private void requireDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Ngày đặt lịch là bắt buộc");
    }
  }

  private String normalizeOptional(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}
