package com.graduation.project.staff.entity;

import com.graduation.project.user.entity.User;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "staff")
public class Staff {

  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", unique = true)
  private User user;

  @Column(name = "full_name", nullable = false, length = 150)
  private String fullName;

  @Column(length = 20)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_type", nullable = false, length = 50)
  private StaffRoleType roleType;

  @Column(name = "license_number", length = 100)
  private String licenseNumber;

  @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
  private BigDecimal baseSalary;

  @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal commissionRate;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (baseSalary == null) {
      baseSalary = BigDecimal.ZERO;
    }

    if (commissionRate == null) {
      commissionRate = BigDecimal.ZERO;
    }

    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
