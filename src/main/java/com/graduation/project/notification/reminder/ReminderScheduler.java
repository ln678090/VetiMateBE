package com.graduation.project.notification.reminder;

import com.graduation.project.notification.config.ZaloReminderProperties;
import com.graduation.project.notification.gateway.ZaloGatewayClient;
import com.graduation.project.notification.gateway.ZaloGatewayException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ReminderScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReminderScheduler.class);

  private static final Duration PROCESSING_LOCK = Duration.ofMinutes(2);

  private static final Duration QUOTA_RECHECK_DELAY = Duration.ofMinutes(1);

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final ZaloReminderRepository reminderRepository;
  private final DailyQuotaService dailyQuotaService;
  private final ZaloGatewayClient zaloGatewayClient;
  private final ZaloReminderProperties properties;
  private final TransactionTemplate transactionTemplate;

  public ReminderScheduler(
      ZaloReminderRepository reminderRepository,
      DailyQuotaService dailyQuotaService,
      ZaloGatewayClient zaloGatewayClient,
      ZaloReminderProperties properties,
      TransactionTemplate transactionTemplate) {
    this.reminderRepository = reminderRepository;
    this.dailyQuotaService = dailyQuotaService;
    this.zaloGatewayClient = zaloGatewayClient;
    this.properties = properties;
    this.transactionTemplate = transactionTemplate;
  }

  @Scheduled(fixedDelayString = "${app.zalo-reminder.scheduler-delay:10s}")
  public void dispatchNextReminder() {
    Instant now = Instant.now();

    Optional<ZaloReminder> claimed = claimNextReminder(now);

    if (claimed.isEmpty()) {
      return;
    }

    ZaloReminder reminder = claimed.get();

    LocalDate quotaDate = LocalDate.now(properties.timezone());

    if (!dailyQuotaService.reserve(quotaDate)) {
      defer(reminder.getId(), now.plus(QUOTA_RECHECK_DELAY));
      return;
    }

    send(reminder);
  }

  private Optional<ZaloReminder> claimNextReminder(Instant now) {
    Optional<ZaloReminder> result =
        transactionTemplate.execute(
            status ->
                reminderRepository
                    .findNextDueForUpdate(now)
                    .map(
                        reminder -> {
                          reminder.markProcessing(now.plus(PROCESSING_LOCK));
                          return reminder;
                        }));

    return result == null ? Optional.empty() : result;
  }

  private void send(ZaloReminder reminder) {
    try {
      zaloGatewayClient.sendText(reminder.getPhone(), buildMessage(reminder));

      markSent(reminder.getId());
    } catch (ZaloGatewayException exception) {
      handleGatewayFailure(reminder.getId(), exception);
    } catch (RuntimeException exception) {
      markFailed(reminder.getId(), "Unexpected dispatch failure");

      log.error("Unexpected reminder failure, reminderId={}", reminder.getId(), exception);
    }
  }

  private String buildMessage(ZaloReminder reminder) {
    String description =
        switch (reminder.getReminderType()) {
          case APPOINTMENT -> "có lịch khám";
          case VACCINATION -> "đã đến hạn tiêm phòng";
          case FOLLOW_UP -> "đã đến hạn tái khám";
        };

    return """
        PetCare xin chào. Bé %s %s vào ngày %s.

        Xem hoặc đặt lịch tại:
        %s

        Nếu không muốn nhận thông báo, vui lòng liên hệ PetCare.
        """
        .formatted(
            reminder.getPetName(),
            description,
            reminder.getDueDate().format(DATE_FORMATTER),
            buildBookingUrl(reminder.getPetId()))
        .trim();
  }

  private String buildBookingUrl(UUID petId) {
    String origin = properties.publicAppUrl().toString().replaceAll("/+$", "");

    return origin + "/booking?petId=" + petId;
  }

  private void markSent(UUID reminderId) {
    transactionTemplate.executeWithoutResult(
        status ->
            reminderRepository
                .findById(reminderId)
                .ifPresent(reminder -> reminder.markSent(Instant.now())));
  }

  private void defer(UUID reminderId, Instant nextAttemptAt) {
    transactionTemplate.executeWithoutResult(
        status ->
            reminderRepository
                .findById(reminderId)
                .ifPresent(reminder -> reminder.deferUntil(nextAttemptAt)));
  }

  private void handleGatewayFailure(UUID reminderId, ZaloGatewayException exception) {
    transactionTemplate.executeWithoutResult(
        status ->
            reminderRepository
                .findById(reminderId)
                .ifPresent(
                    reminder -> {
                      if (!exception.isRetryable()) {
                        reminder.markFailed(safeError(exception));
                        return;
                      }

                      reminder.scheduleRetry(
                          Instant.now().plus(retryDelay(reminder.getAttemptCount())),
                          safeError(exception));
                    }));

    log.warn(
        "Zalo gateway failure, reminderId={}, status={}", reminderId, exception.getStatusCode());
  }

  private void markFailed(UUID reminderId, String error) {
    transactionTemplate.executeWithoutResult(
        status ->
            reminderRepository
                .findById(reminderId)
                .ifPresent(reminder -> reminder.markFailed(error)));
  }

  private Duration retryDelay(int attemptCount) {
    return switch (attemptCount) {
      case 0 -> Duration.ofMinutes(5);
      case 1 -> Duration.ofMinutes(15);
      case 2 -> Duration.ofHours(1);
      case 3 -> Duration.ofHours(6);
      default -> Duration.ofHours(24);
    };
  }

  private String safeError(ZaloGatewayException exception) {
    return "Gateway HTTP status: " + exception.getStatusCode();
  }
}
