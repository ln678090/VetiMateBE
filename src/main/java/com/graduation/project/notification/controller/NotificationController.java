package com.graduation.project.notification.controller;

import com.graduation.project.notification.dto.NotificationDto;
import com.graduation.project.notification.service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> getNotifications(JwtAuthenticationToken auth) {
    // If user is staff, they might need both personal and system (null userId) notifications,
    // but for now we fetch based on role. If they are staff, fetch null (system) + personal?
    // Let's assume STAFF only get system (null) and Users get personal (userId).
    UUID userId = null;
    if (auth.getAuthorities().stream()
        .noneMatch(
            a ->
                a.getAuthority().startsWith("ROLE_STAFF")
                    || a.getAuthority().startsWith("ROLE_ADMIN"))) {
      userId = UUID.fromString(auth.getName());
    }

    return ResponseEntity.ok(notificationService.getUserNotifications(userId));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<Map<String, Long>> getUnreadCount(JwtAuthenticationToken auth) {
    UUID userId = null;
    if (auth.getAuthorities().stream()
        .noneMatch(
            a ->
                a.getAuthority().startsWith("ROLE_STAFF")
                    || a.getAuthority().startsWith("ROLE_ADMIN"))) {
      userId = UUID.fromString(auth.getName());
    }
    return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(userId)));
  }

  @PutMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable UUID id, JwtAuthenticationToken auth) {
    UUID userId = null;
    if (auth.getAuthorities().stream()
        .noneMatch(
            a ->
                a.getAuthority().startsWith("ROLE_STAFF")
                    || a.getAuthority().startsWith("ROLE_ADMIN"))) {
      userId = UUID.fromString(auth.getName());
    }
    notificationService.markAsRead(id, userId);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/read-all")
  public ResponseEntity<Void> markAllAsRead(JwtAuthenticationToken auth) {
    UUID userId = null;
    if (auth.getAuthorities().stream()
        .noneMatch(
            a ->
                a.getAuthority().startsWith("ROLE_STAFF")
                    || a.getAuthority().startsWith("ROLE_ADMIN"))) {
      userId = UUID.fromString(auth.getName());
    }
    notificationService.markAllAsRead(userId);
    return ResponseEntity.ok().build();
  }
}
