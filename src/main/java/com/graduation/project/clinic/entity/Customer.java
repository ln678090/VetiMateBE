package com.graduation.project.clinic.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clinic_customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

  @Id
  @UuidV7
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Link tới User nếu khách có tài khoản; nullable cho khách vãng lai (lễ tân
   * tạo).
   */
  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(name = "phone", nullable = false, length = 20)
  private String phone;

  @Column(name = "email", length = 150)
  private String email;

  @Column(name = "address", length = 255)
  private String address;

  @Column(name = "note", length = 500)
  private String note;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<Pet> pets = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
