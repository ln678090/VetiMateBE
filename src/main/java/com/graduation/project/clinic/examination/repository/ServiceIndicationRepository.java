package com.graduation.project.clinic.examination.repository;

import com.graduation.project.clinic.examination.entity.ServiceIndication;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceIndicationRepository extends JpaRepository<ServiceIndication, UUID> {

  @EntityGraph(attributePaths = { "service" })
  List<ServiceIndication> findAllByMedicalRecordIdOrderByCreatedAtAsc(UUID medicalRecordId);

  @Query(value = """
      SELECT si.*
      FROM service_indications si
      JOIN medical_records mr
        ON mr.id = si.medical_record_id
      JOIN staff st
        ON st.id = mr.doctor_id
      WHERE si.id = :indicationId
        AND st.user_id = :userId
      FOR UPDATE
      """, nativeQuery = true)
  Optional<ServiceIndication> findOwnedByIdForUpdate(
      @Param("indicationId") UUID indicationId,
      @Param("userId") UUID userId);

  boolean existsByMedicalRecordIdAndServiceIdAndStatus(
      UUID medicalRecordId,
      UUID serviceId,
      com.graduation.project.clinic.examination.entity.ServiceIndicationStatus status);

}
