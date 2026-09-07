package com.graduation.project.notification.repository;

import com.graduation.project.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findByUserIdOrderByCreatedAtDescIdDesc(UUID userId, Pageable pageable);

  Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

  long countByUserIdAndIsReadFalse(UUID userId);

  @Modifying(clearAutomatically = true)
  @Query(
      """
      UPDATE Notification notification
      SET notification.isRead = true,
          notification.readAt = :readAt
      WHERE notification.userId = :userId
        AND notification.isRead = false
      """)
  int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") Instant readAt);
}
