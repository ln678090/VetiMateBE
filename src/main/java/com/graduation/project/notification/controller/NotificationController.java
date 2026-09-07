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

  private UUID getTargetUserId(Authentication auth) {
    if (auth.getAuthorities().stream()
        .anyMatch(
            a ->
                a.getAuthority().equals("ROLE_SHOP_STAFF")
                    || a.getAuthority().equals("ROLE_ADMIN")
                    || a.getAuthority().equals("ROLE_MANAGER"))) {
      return null;
    }
    return SecurityUtils.currentUserId(auth);
  }

  @GetMapping
  public List<NotificationDto> getNotifications(Authentication authentication) {
    UUID userId = getTargetUserId(authentication);
    return notificationService.getUserNotifications(userId);
  }

  @GetMapping("/unread-count")
  public UnreadNotificationCountDto getUnreadCount(Authentication authentication) {
    UUID userId = getTargetUserId(authentication);
    return new UnreadNotificationCountDto(notificationService.getUnreadCount(userId));
  }

  @PatchMapping("/{notificationId}/read")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAsRead(
      @PathVariable("notificationId") UUID notificationId, Authentication authentication) {
    UUID userId = getTargetUserId(authentication);
    notificationService.markAsRead(notificationId, userId);
  }

  @PatchMapping("/read-all")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void markAllAsRead(Authentication authentication) {
    UUID userId = getTargetUserId(authentication);
    notificationService.markAllAsRead(userId);
  }
}
