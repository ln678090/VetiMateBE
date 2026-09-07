package com.graduation.project.notification.reminder;

import com.graduation.project.notification.config.ZaloReminderProperties;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentReminderService {

  private static final String FIND_PHONE_SQL =
      """
      SELECT TRIM(users.phone)
      FROM clinic_customers customer
      JOIN users ON users.id = customer.user_id
      WHERE customer.id = ?
        AND NULLIF(TRIM(users.phone), '') IS NOT NULL
      """;

  private final JdbcTemplate jdbcTemplate;
  private final ZaloReminderRepository reminderRepository;
  private final ZaloReminderProperties properties;

  public AppointmentReminderService(
      JdbcTemplate jdbcTemplate,
      ZaloReminderRepository reminderRepository,
      ZaloReminderProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.reminderRepository = reminderRepository;
    this.properties = properties;
  }

  @Transactional
  public Optional<ZaloReminder> schedule(
      UUID appointmentId, UUID customerId, UUID petId, String petName, Instant appointmentStart) {
    Optional<ZaloReminder> existingReminder = reminderRepository.findByAppointmentId(appointmentId);

    if (existingReminder.isPresent()) {
      return existingReminder;
    }

    List<String> phones =
        jdbcTemplate.query(
            FIND_PHONE_SQL, (resultSet, rowNumber) -> resultSet.getString(1), customerId);

    if (phones.isEmpty()) {
      return Optional.empty();
    }

    ZaloReminder reminder =
        ZaloReminder.createAppointment(
            appointmentId,
            customerId,
            petId,
            phones.getFirst(),
            petName,
            appointmentStart,
            properties.timezone());

    return Optional.of(reminderRepository.saveAndFlush(reminder));
  }

  @Transactional(readOnly = true)
  public Optional<ZaloReminder> findByAppointmentId(UUID appointmentId) {
    return reminderRepository.findByAppointmentId(appointmentId);
  }
}
