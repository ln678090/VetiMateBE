package com.graduation.project.notification.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
  private UUID id;
  private String title;
  private String message;
  private String link;
  private Boolean isRead;
  private Instant createdAt;
}
