package com.graduation.project.notification.service.impl;

import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.notification.dto.NotificationDto;
import com.graduation.project.notification.entity.Notification;
import com.graduation.project.notification.repository.NotificationRepository;
import com.graduation.project.notification.service.NotificationService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private static final int MAX_RECENT_ITEMS = 100;
  private static final int MAX_TITLE_LENGTH = 255;
  private static final int MAX_LINK_LENGTH = 255;
  private static final Duration UNREAD_CACHE_TTL = Duration.ofMinutes(5);
  private static final String UNREAD_KEY_PREFIX = "vetimate:notification:unread:";

  private final NotificationRepository notificationRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  @Transactional
  public void createNotification(UUID userId, String title, String message, String link) {
    if (userId != null) {
      requireUserId(userId);
    }

    String safeTitle = truncate(normalizeRequired(title, "Tiêu đề thông báo"), MAX_TITLE_LENGTH);
    String safeMessage = normalizeRequired(message, "Nội dung thông báo");
    String safeLink = normalizeInternalLink(link);

    Notification notification =
        Notification.builder()
            .userId(userId)
            .channel("IN_APP")
            .recipient(userId == null ? "SYSTEM" : userId.toString())
            .title(safeTitle)
            .body(safeMessage)
            .link(safeLink)
            .status("SENT")
            .attemptCount(1)
            .isRead(false)
            .sentAt(Instant.now())
            .build();

    notificationRepository.save(notification);

    if (userId != null) {
      runAfterCommit(() -> evictUnreadCache(userId));
    }
  }

  @Override
  public List<NotificationDto> getUserNotifications(UUID userId) {
    requireUserId(userId);

    return notificationRepository
        .findByUserIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(0, MAX_RECENT_ITEMS))
        .stream()
        .map(this::toDto)
        .toList();
  }


  @Override
  @Transactional
  public void markAsRead(UUID notificationId, UUID userId) {
    if (notificationId == null) {
      throw new IllegalArgumentException("Notification ID là bắt buộc");
    }

    Notification notification;
    if (userId == null) {
      notification = notificationRepository.findById(notificationId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    } else {
      notification = notificationRepository.findByIdAndUserId(notificationId, userId)
          .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));
    }

    if (Boolean.TRUE.equals(notification.getIsRead())) {
      return;
    }

    notification.setIsRead(true);
    notification.setReadAt(Instant.now());

    notificationRepository.save(notification);

    if (userId != null) {
      runAfterCommit(() -> evictUnreadCache(userId));
    }
  }

  @Override
  @Transactional
  public void markAllAsRead(UUID userId) {
    if (userId == null) {
      notificationRepository.markAllSystemAsRead(Instant.now());
    } else {
      notificationRepository.markAllAsRead(userId, Instant.now());
      runAfterCommit(() -> cacheUnreadCount(userId, 0L));
    }
  }

  @Override
  @Transactional(readOnly = true)
  public long getUnreadCount(UUID userId) {
    if (userId == null) {
      return notificationRepository.countByUserIdIsNullAndIsReadFalse();
    }

    Long cachedCount = readUnreadCountFromCache(userId);
    if (cachedCount != null) {
      return cachedCount;
    }

    long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);
    cacheUnreadCount(userId, unreadCount);

    return unreadCount;
  }

  private NotificationDto toDto(Notification notification) {
    return NotificationDto.builder()
        .id(notification.getId())
        .title(notification.getTitle())
        .message(notification.getBody())
        .link(notification.getLink())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }

  /**
   * Chỉ chấp nhận đường dẫn nội bộ.
   *
   * <p>Hợp lệ: /profile/pets/123 /profile/orders?orderId=123
   *
   * <p>Không hợp lệ: https://example.com //example.com javascript:alert(1)
   */
  private String normalizeInternalLink(String link) {
    if (link == null || link.isBlank()) {
      return null;
    }

    String normalized = link.trim();

    if (!normalized.startsWith("/") || normalized.startsWith("//")) {
      throw new IllegalArgumentException("Link thông báo phải là đường dẫn nội bộ");
    }

    return truncate(normalized, MAX_LINK_LENGTH);
  }

  private void requireUserId(UUID userId) {
    if (userId == null) {
      throw new IllegalArgumentException("User ID là bắt buộc");
    }
  }

  private String normalizeRequired(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(fieldName + " là bắt buộc");
    }

    return value.trim();
  }

  private String truncate(String value, int maximumLength) {
    if (value.length() <= maximumLength) {
      return value;
    }

    return value.substring(0, maximumLength);
  }

  private String unreadCacheKey(UUID userId) {
    return UNREAD_KEY_PREFIX + userId;
  }

  private Long readUnreadCountFromCache(UUID userId) {
    try {
      String cachedValue = redisTemplate.opsForValue().get(unreadCacheKey(userId));

      if (cachedValue == null) {
        return null;
      }

      return Long.parseLong(cachedValue);
    } catch (RuntimeException exception) {
      log.warn("Không thể đọc unread count từ Redis cho user {}", userId);
      return null;
    }
  }

  private void cacheUnreadCount(UUID userId, long unreadCount) {
    try {
      redisTemplate
          .opsForValue()
          .set(unreadCacheKey(userId), Long.toString(unreadCount), UNREAD_CACHE_TTL);
    } catch (RuntimeException exception) {
      log.warn("Không thể cache unread count vào Redis cho user {}", userId);
    }
  }

  private void evictUnreadCache(UUID userId) {
    try {
      redisTemplate.delete(unreadCacheKey(userId));
    } catch (RuntimeException exception) {
      log.warn("Không thể xóa unread cache Redis cho user {}", userId);
    }
  }

  private void runAfterCommit(Runnable action) {
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      action.run();
      return;
    }

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            action.run();
          }
        });
  }
}
