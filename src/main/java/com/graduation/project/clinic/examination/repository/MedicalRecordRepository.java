package com.graduation.project.clinic.examination.repository;

import com.graduation.project.clinic.examination.entity.MedicalRecord;
import com.graduation.project.clinic.examination.entity.MedicalRecordStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
  Optional<MedicalRecord> findByAppointmentId(UUID appointmentId);

  @Query(
      value =
          """
      SELECT mr.*
      FROM medical_records mr
      JOIN staff st
        ON st.id = mr.doctor_id
      WHERE mr.id = :recordId
        AND st.user_id = :userId
      """,
      nativeQuery = true)
  Optional<MedicalRecord> findOwnedById(
      @Param("recordId") UUID recordId, @Param("userId") UUID userId);

  @Query(
      """
      select medicalRecord
      from MedicalRecord medicalRecord
      join fetch medicalRecord.appointment appointment
      join fetch medicalRecord.pet pet
      join fetch medicalRecord.doctor doctor
      left join fetch doctor.user
      where medicalRecord.id = :medicalRecordId
      """)
  Optional<MedicalRecord> findByIdFull(@Param("medicalRecordId") UUID medicalRecordId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select medicalRecord
      from MedicalRecord medicalRecord
      join fetch medicalRecord.appointment appointment
      join fetch medicalRecord.pet pet
      join fetch medicalRecord.doctor doctor
      left join fetch doctor.user
      where medicalRecord.id = :medicalRecordId
      """)
  Optional<MedicalRecord> findByIdForUpdate(@Param("medicalRecordId") UUID medicalRecordId);

  @EntityGraph(attributePaths = {"appointment", "pet", "doctor", "doctor.user"})
  @Query(
      value =
          """
      select medicalRecord
      from MedicalRecord medicalRecord
      where medicalRecord.doctor.user.id = :doctorUserId
        and medicalRecord.status = :status
      """,
      countQuery =
          """
      select count(medicalRecord)
      from MedicalRecord medicalRecord
      where medicalRecord.doctor.user.id = :doctorUserId
        and medicalRecord.status = :status
      """)
  Page<MedicalRecord> findHistoryByDoctorUserId(
      @Param("doctorUserId") UUID doctorUserId,
      @Param("status") MedicalRecordStatus status,
      Pageable pageable);

  @EntityGraph(attributePaths = {"appointment", "pet", "doctor"})
  Optional<MedicalRecord> findByAppointment_Id(UUID appointmentId);

  @EntityGraph(attributePaths = {"appointment", "pet", "doctor"})
  @Query(
      """
      SELECT medicalRecord
      FROM MedicalRecord medicalRecord
      WHERE medicalRecord.id = :medicalRecordId
      """)
  Optional<MedicalRecord> findDetailedById(@Param("medicalRecordId") UUID medicalRecordId);

  @EntityGraph(attributePaths = {"appointment", "appointment.pet", "doctor"})
  Page<MedicalRecord> findByDoctor_UserIdAndStatus(
      UUID userId, MedicalRecordStatus status, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select medicalRecord
      from MedicalRecord medicalRecord
      join fetch medicalRecord.appointment appointment
      join fetch medicalRecord.pet pet
      join fetch medicalRecord.doctor doctor
      where medicalRecord.id = :medicalRecordId
      """)
  Optional<MedicalRecord> findDetailedByIdForUpdate(@Param("medicalRecordId") UUID medicalRecordId);
}
