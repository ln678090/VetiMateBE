package com.graduation.project.user.entity;

import com.graduation.project.auth.entity.Role;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

  @Id
  @UuidV7 // ← Sinh UUIDv7 từ Java
  @Column(columnDefinition = "uuid", updatable = false, nullable = false)
  private UUID id;

  @Column(unique = true, nullable = false, length = 50)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(unique = true, length = 100)
  private String email;

  @Column(name = "full_name", length = 100)
  private String fullName;

  @Column(nullable = false)
  private Boolean enabled = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "phone", length = 20)
  private String phone;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private List<Role> roles = new ArrayList<>();

  public Boolean getIsEnabled() {
    return enabled;
  }
}
