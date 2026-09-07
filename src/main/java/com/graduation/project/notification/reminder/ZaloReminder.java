package com.graduation.project.notification.reminder;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "zalo_reminders",
    indexes = {
      @Index(name = "idx_zalo_reminders_dispatch", columnList = "status,due_date,next_attempt_at")
    },
    uniqueConstraints = {
      @UniqueConstraint(name = "uq_zalo_reminders_idempotency_key", columnNames = "idempotency_key")
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ZaloReminder {

  private static final int MAXIMUM_ATTEMPTS = 5;

  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(name = "appointment_id", updatable = false)
  private UUID appointmentId;

  @Column(name = "customer_id", nullable = false, updatable = false)
  private UUID customerId;

  @Column(name = "pet_id", nullable = false, updatable = false)
  private UUID petId;

  @Column(nullable = false, length = 20)
  private String phone;

  @Column(name = "pet_name", nullable = false, length = 100)
  private String petName;

  @Enumerated(EnumType.STRING)
  @Column(name = "reminder_type", nullable = false, length = 30)
  private ReminderType reminderType;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReminderStatus status;

  @Column(
      name = "idempotency_key",
      nullable = false,
      unique = true,
      length = 200,
      updatable = false)
  private String idempotencyKey;

  @Column(name = "attempt_count", nullable = false)
  private int attemptCount;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "last_error")
  private String lastError;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public static ZaloReminder createAppointment(
      UUID appointmentId,
      UUID customerId,
      UUID petId,
      String phone,
      String petName,
      Instant appointmentStart,
      ZoneId timezone) {
    Instant now = Instant.now();
    Instant scheduledAt = appointmentStart.minus(Duration.ofHours(24));

    ZaloReminder reminder = new ZaloReminder();
    reminder.appointmentId = appointmentId;
    reminder.customerId = customerId;
    reminder.petId = petId;
    reminder.phone = phone;
    reminder.petName = petName;
    reminder.reminderType = ReminderType.APPOINTMENT;
    reminder.dueDate = appointmentStart.atZone(timezone).toLocalDate();
    reminder.status = ReminderStatus.PENDING;
    reminder.idempotencyKey = "APPOINTMENT:" + appointmentId;
    reminder.attemptCount = 0;
    reminder.nextAttemptAt = scheduledAt.isAfter(now) ? scheduledAt : now;

    return reminder;
  }

  public static ZaloReminder create(
      UUID customerId,
      UUID petId,
      String phone,
      String petName,
      ReminderType reminderType,
      LocalDate dueDate) {
    ZaloReminder reminder = new ZaloReminder();

    reminder.customerId = customerId;
    reminder.petId = petId;
    reminder.phone = phone;
    reminder.petName = petName;
    reminder.reminderType = reminderType;
    reminder.dueDate = dueDate;
    reminder.status = ReminderStatus.PENDING;
    reminder.attemptCount = 0;
    reminder.nextAttemptAt = Instant.now();
    reminder.idempotencyKey = createIdempotencyKey(petId, reminderType, dueDate);

    return reminder;
  }

  public void markProcessing(Instant lockedUntil) {
    this.status = ReminderStatus.PROCESSING;
    this.lockedUntil = lockedUntil;
  }

  public void markSent(Instant sentAt) {
    this.status = ReminderStatus.SENT;
    this.sentAt = sentAt;
    this.lockedUntil = null;
    this.lastError = null;
  }

  public void scheduleRetry(Instant nextAttemptAt, String errorMessage) {
    this.attemptCount++;
    this.lockedUntil = null;
    this.lastError = truncateError(errorMessage);

    if (this.attemptCount >= MAXIMUM_ATTEMPTS) {
      this.status = ReminderStatus.FAILED;
      return;
    }

    this.status = ReminderStatus.PENDING;
    this.nextAttemptAt = nextAttemptAt;
  }

  public void markFailed(String errorMessage) {
    this.attemptCount++;
    this.status = ReminderStatus.FAILED;
    this.lockedUntil = null;
    this.lastError = truncateError(errorMessage);
  }

  public boolean hasReachedMaximumAttempts() {
    return attemptCount >= MAXIMUM_ATTEMPTS;
  }

  private static String createIdempotencyKey(
      UUID petId, ReminderType reminderType, LocalDate dueDate) {
    return reminderType.name() + ":" + petId + ":" + dueDate;
  }

  private static String truncateError(String errorMessage) {
    if (errorMessage == null) {
      return null;
    }

    int maximumLength = 2_000;

    return errorMessage.length() <= maximumLength
        ? errorMessage
        : errorMessage.substring(0, maximumLength);
  }

  public void deferUntil(Instant nextAttemptAt) {
    this.status = ReminderStatus.PENDING;
    this.nextAttemptAt = nextAttemptAt;
    this.lockedUntil = null;
  }

  @PrePersist
  private void beforeInsert() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  private void beforeUpdate() {
    this.updatedAt = Instant.now();
  }
}
