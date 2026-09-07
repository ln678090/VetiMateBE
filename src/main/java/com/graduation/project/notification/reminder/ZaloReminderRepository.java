package com.graduation.project.notification.reminder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZaloReminderRepository extends JpaRepository<ZaloReminder, UUID> {

  boolean existsByIdempotencyKey(String idempotencyKey);

  @Query(
      value =
          """
      SELECT reminder.*
      FROM zalo_reminders reminder
      WHERE reminder.next_attempt_at <= :now
        AND (
            reminder.status = 'PENDING'
            OR (
                reminder.status = 'PROCESSING'
                AND reminder.locked_until < :now
            )
        )
      ORDER BY reminder.next_attempt_at ASC
      FOR UPDATE SKIP LOCKED
      LIMIT 1
      """,
      nativeQuery = true)
  Optional<ZaloReminder> findNextDueForUpdate(@Param("now") Instant now);

  Optional<ZaloReminder> findByAppointmentId(UUID appointmentId);
}
