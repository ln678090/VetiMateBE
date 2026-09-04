package com.graduation.project.clinic.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "clinic_appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

  @Id
  @UuidV7
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pet_id", nullable = false)
  private Pet pet;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private ClinicService service;

  @Column(name = "start_at", nullable = false)
  private Instant startAt;

  @Column(name = "end_at", nullable = false)
  private Instant endAt;

  /** Snapshot thời lượng lúc đặt (phút). */
  @Column(name = "duration_min", nullable = false)
  private Integer durationMin;

  /** Snapshot giá lúc đặt. */
  @Column(name = "price_snapshot", nullable = false, precision = 12, scale = 2)
  private BigDecimal priceSnapshot;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private AppointmentStatus status = AppointmentStatus.SCHEDULED;

  @Column(name = "note", length = 500)
  private String note;

  @Column(name = "is_called_to_confirm", nullable = false)
  @Builder.Default
  private Boolean isCalledToConfirm = false;

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
