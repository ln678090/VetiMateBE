package com.graduation.project.clinic.examination.entity;

import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.utils.annotation.UuidV7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
public class Prescription {

  @Id
  @UuidV7
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "medical_record_id", nullable = false)
  private MedicalRecord medicalRecord;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "medicine_id", nullable = false)
  private Medicine medicine;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal quantity;

  @Column(length = 200)
  private String dosage;

  @Column(name = "duration_days")
  private Integer durationDays;

  @Column(length = 500)
  private String note;
}
