package com.graduation.project.clinic.examination.repository;

import com.graduation.project.clinic.examination.entity.Prescription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

  @EntityGraph(attributePaths = "medicine")
  List<Prescription> findAllByMedicalRecord_IdOrderByIdAsc(UUID medicalRecordId);

  long countByMedicalRecord_Id(UUID medicalRecordId);

  void deleteAllByMedicalRecord_Id(UUID medicalRecordId);
}
