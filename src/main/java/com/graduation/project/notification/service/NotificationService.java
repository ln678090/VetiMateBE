package com.graduation.project.notification.service;

import com.graduation.project.notification.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createNotification(UUID userId, String title, String message, String link);
    List<NotificationDto> getUserNotifications(UUID userId);
    void markAsRead(UUID id, UUID userId);
    void markAllAsRead(UUID userId);
    long getUnreadCount(UUID userId);
}
