package com.graduation.project.auth.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @UuidV7 // ← Sinh UUIDv7 từ Java
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(unique = true, nullable = false, length = 50)
  private String name; // ROLE_ADMIN, ROLE_USER

  private String description;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;
}
