package com.graduation.project.notification.reminder;

import com.graduation.project.notification.config.ZaloReminderProperties;
import java.time.LocalDate;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DailyQuotaService {

  private static final String RESERVE_QUOTA_SQL =
      """
      INSERT INTO zalo_daily_quotas (
          quota_date,
          attempt_count,
          last_attempt_at
      )
      VALUES (?, 1, CURRENT_TIMESTAMP)
      ON CONFLICT (quota_date)
      DO UPDATE SET
          attempt_count =
              zalo_daily_quotas.attempt_count + 1,
          last_attempt_at = CURRENT_TIMESTAMP
      WHERE zalo_daily_quotas.attempt_count < ?
        AND (
            zalo_daily_quotas.last_attempt_at IS NULL
            OR zalo_daily_quotas.last_attempt_at
               <= CURRENT_TIMESTAMP
                  - make_interval(secs => ?)
        )
      RETURNING attempt_count
      """;

  private final JdbcTemplate jdbcTemplate;
  private final ZaloReminderProperties properties;

  public DailyQuotaService(JdbcTemplate jdbcTemplate, ZaloReminderProperties properties) {
    this.jdbcTemplate = jdbcTemplate;
    this.properties = properties;
  }

  /**
   * Giữ một lượt gửi trước khi gọi Go gateway.
   *
   * @return true nếu còn quota và đã đủ khoảng cách gửi
   */
  @Transactional
  public boolean reserve(LocalDate quotaDate) {
    long configuredSeconds = properties.minimumInterval().getSeconds();

    int minimumIntervalSeconds = Math.toIntExact(Math.max(1L, configuredSeconds));

    try {
      Integer reservedCount =
          jdbcTemplate.queryForObject(
              RESERVE_QUOTA_SQL,
              Integer.class,
              quotaDate,
              properties.dailyLimit(),
              minimumIntervalSeconds);

      return reservedCount != null && reservedCount <= properties.dailyLimit();
    } catch (EmptyResultDataAccessException exception) {
      // Hết quota hoặc chưa đủ khoảng cách giữa hai request.
      return false;
    }
  }
}
