
package com.graduation.project.clinic.examination.repository;

import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

  @EntityGraph(attributePaths = {
      "appointment",
      "pet",
      "doctor"
  })
  Optional<MedicalRecord> findByAppointment_Id(
      UUID appointmentId);

  @EntityGraph(attributePaths = {
      "appointment",
      "pet",
      "doctor"
  })
  @Query("""
      SELECT medicalRecord
      FROM MedicalRecord medicalRecord
      WHERE medicalRecord.id = :medicalRecordId
      """)
  Optional<MedicalRecord> findDetailedById(
      @Param("medicalRecordId") UUID medicalRecordId);

  @EntityGraph(attributePaths = {
      "appointment",
      "appointment.pet",
      "doctor"
  })
  Page<MedicalRecord> findByDoctor_UserIdAndStatus(
      UUID userId,
      MedicalRecordStatus status,
      Pageable pageable);
}
