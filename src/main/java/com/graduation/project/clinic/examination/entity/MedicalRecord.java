package com.graduation.project.clinic.examination.entity;

import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.enums.PetHealthStatus;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medical_records", uniqueConstraints = @UniqueConstraint(name = "uk_medical_records_appointment", columnNames = "appointment_id"))
@Getter
@Setter
@NoArgsConstructor
public class MedicalRecord {

  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appointment_id", nullable = false, unique = true)
  private Appointment appointment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "pet_id", nullable = false)
  private Pet pet;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "doctor_id", nullable = false)
  private Staff doctor;

  @Column(columnDefinition = "TEXT")
  private String symptoms;

  @Column(columnDefinition = "TEXT")
  private String diagnosis;

  @Column(name = "treatment_plan", columnDefinition = "TEXT")
  private String treatmentPlan;

  @Column(name = "weight_kg", precision = 6, scale = 2)
  private BigDecimal weightKg;

  @Enumerated(EnumType.STRING)
  @Column(name = "health_status", nullable = false, length = 30)
  private PetHealthStatus healthStatus = PetHealthStatus.MONITORING;

  @Column(name = "doctor_note", columnDefinition = "TEXT")
  private String doctorNote;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private MedicalRecordStatus status = MedicalRecordStatus.IN_PROGRESS;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
