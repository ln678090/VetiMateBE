package com.graduation.project.notification.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.notification.dto.NotificationDto;
import com.graduation.project.notification.dto.UnreadNotificationCountDto;
import com.graduation.project.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public List<NotificationDto> getNotifications(Authentication authentication) {
    UUID userId = SecurityUtils.currentUserId(authentication);

    return notificationService.getUserNotifications(userId);
  }

  @GetMapping("/unread-count")
  public UnreadNotificationCountDto getUnreadCount(Authentication authentication) {
    UUID userId = SecurityUtils.currentUserId(authentication);

    return new UnreadNotificationCountDto(notificationService.getUnreadCount(userId));
  }

  @PatchMapping("/{notificationId}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsRead(@PathVariable UUID notificationId, Authentication authentication) {
    UUID userId = SecurityUtils.currentUserId(authentication);

    notificationService.markAsRead(notificationId, userId);
  }

  @PatchMapping("/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllAsRead(Authentication authentication) {
    UUID userId = SecurityUtils.currentUserId(authentication);

    notificationService.markAllAsRead(userId);
  }
}
