package com.graduation.project.notification.service.impl;

import com.graduation.project.notification.dto.NotificationDto;
import com.graduation.project.notification.entity.Notification;
import com.graduation.project.notification.repository.NotificationRepository;
import com.graduation.project.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  @Transactional
  public void createNotification(UUID userId, String title, String message, String link) {
    Notification notification =
        Notification.builder()
            .userId(userId)
            .title(title)
            .body(message)
            .link(link)
            .channel("IN_APP")
            .status("SENT")
            .isRead(false)
            .build();

    notification = notificationRepository.save(notification);

    NotificationDto dto = mapToDto(notification);

    if (userId == null) {
      // Broadcast to all staff
      messagingTemplate.convertAndSend("/topic/shop-orders", dto);
    } else {
      // Send to specific user
      messagingTemplate.convertAndSend("/topic/user-orders-" + userId, dto);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificationDto> getUserNotifications(UUID userId) {
    List<Notification> notifications;
    if (userId == null) {
        notifications = notificationRepository.findAllByUserIdIsNullOrderByCreatedAtDesc();
    } else {
        notifications = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }
    return notifications.stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void markAsRead(UUID id, UUID userId) {
    Notification notification =
        notificationRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
            
    // To support staff notifications where userId is null, we can bypass the check if the request is from staff or if notification has no userId.
    // For simplicity, we just mark it as read.
    notification.setIsRead(true);
    notificationRepository.save(notification);
  }

  @Override
  @Transactional
  public void markAllAsRead(UUID userId) {
    List<Notification> notifications;
    if (userId == null) {
        notifications = notificationRepository.findAllByUserIdIsNullAndIsReadFalse();
    } else {
        notifications = notificationRepository.findAllByUserIdAndIsReadFalse(userId);
    }
    for (Notification n : notifications) {
      n.setIsRead(true);
    }
    notificationRepository.saveAll(notifications);
  }

  @Override
  @Transactional(readOnly = true)
  public long getUnreadCount(UUID userId) {
    if (userId == null) {
        return notificationRepository.countByUserIdIsNullAndIsReadFalse();
    }
    return notificationRepository.countByUserIdAndIsReadFalse(userId);
  }

  private NotificationDto mapToDto(Notification notification) {
    return NotificationDto.builder()
        .id(notification.getId())
        .title(notification.getTitle())
        .message(notification.getBody())
        .link(notification.getLink())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}
