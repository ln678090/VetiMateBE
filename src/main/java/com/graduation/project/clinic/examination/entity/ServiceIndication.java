package com.graduation.project.clinic.examination.entity;

import com.graduation.project.clinic.entity.ClinicService;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_indications")
public class ServiceIndication {

  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "medical_record_id", nullable = false)
  private MedicalRecord medicalRecord;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private ClinicService service;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ServiceIndicationStatus status = ServiceIndicationStatus.PENDING;

  @Column(name = "result_note", columnDefinition = "TEXT")
  private String resultNote;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (status == null) {
      status = ServiceIndicationStatus.PENDING;
    }

    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
