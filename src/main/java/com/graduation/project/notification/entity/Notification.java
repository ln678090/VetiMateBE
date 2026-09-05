package com.graduation.project.notification.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

  @Id
  @UuidV7
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "template_id")
  private UUID templateId;

  @Column(name = "channel", nullable = false, length = 20)
  private String channel;

  @Column(name = "recipient", length = 255)
  private String recipient;

  @Column(name = "title", length = 255)
  private String title;

  @Column(name = "body", nullable = false)
  private String body;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "attempt_count", nullable = false)
  @Builder.Default
  private Integer attemptCount = 0;

  @Builder.Default
  @Column(name = "is_read", nullable = false)
  private Boolean isRead = false;

  @Column(name = "scheduled_at")
  private Instant scheduledAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "read_at")
  private Instant readAt;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "link", length = 255)
  private String link;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();

    if (this.createdAt == null) {
      this.createdAt = now;
    }

    if (this.updatedAt == null) {
      this.updatedAt = now;
    }

    if (this.attemptCount == null) {
      this.attemptCount = 0;
    }

    if (this.status == null) {
      this.status = "PENDING";
    }
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
