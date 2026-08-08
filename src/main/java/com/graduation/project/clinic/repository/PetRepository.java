package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {

  // JOIN FETCH chống N+1 khi flatten customerName ra DTO
  @Query("SELECT p FROM Pet p JOIN FETCH p.customer WHERE p.id = :id")
  Optional<Pet> findByIdWithCustomer(@Param("id") UUID id);

  @Query(value = "SELECT p FROM Pet p JOIN FETCH p.customer WHERE p.customer.id = :customerId", countQuery = "SELECT COUNT(p) FROM Pet p WHERE p.customer.id = :customerId")
  Page<Pet> findByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

  // Thêm method để chỉ lấy pet chưa bị xóa:
  List<Pet> findByCustomerIdAndDeletedAtIsNull(UUID customerId);

  Optional<Pet> findByIdAndDeletedAtIsNull(UUID id);

  // Query để đếm pet active của customer
  long countByCustomerIdAndDeletedAtIsNull(UUID customerId);
}
