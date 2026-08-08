package com.graduation.project.clinic.entity;

import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "clinic_pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet {

  @Id
  @UuidV7
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  /** DOG / CAT / OTHER — validate ở DTO. */
  @Enumerated(EnumType.STRING)
  @Column(name = "species", nullable = false, length = 10)
  private PetSpecies species;

  @Column(name = "breed", length = 100)
  private String breed;

  /** MALE / FEMALE / UNKNOWN. */
  @Column(name = "gender", length = 10)
  private String gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "weight_kg", precision = 6, scale = 2)
  private BigDecimal weightKg;

  @Column(name = "note", length = 500)
  private String note;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
  @Column(name = "deleted_at")
  private Instant deletedAt;

  // Getter/Setter (hoặc dùng @Data của Lombok)
  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  // Helper method
  public boolean isDeleted() {
    return deletedAt != null;
  }

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
