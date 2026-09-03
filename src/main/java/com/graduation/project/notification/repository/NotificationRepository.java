package com.graduation.project.notification.repository;

import com.graduation.project.notification.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

  long countByUserIdAndReadAtIsNull(UUID userId);
}
