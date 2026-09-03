package com.graduation.project.notification.service.impl;

import com.graduation.project.notification.dto.NotificationDto;
import com.graduation.project.notification.service.NotificationService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

  @Override
  public void createNotification(UUID userId, String title, String message, String link) {}

  @Override
  public List<NotificationDto> getUserNotifications(UUID userId) {
    return Collections.emptyList();
  }

  @Override
  public void markAsRead(UUID id, UUID userId) {}

  @Override
  public void markAllAsRead(UUID userId) {}

  @Override
  public long getUnreadCount(UUID userId) {
    return 0;
  }
}
