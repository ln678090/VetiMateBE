package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

  Optional<Customer> findByUserId(UUID userId);

  Optional<Customer> findByPhone(String phone);

  boolean existsByPhone(String phone);

  @Query("""
      SELECT c FROM Customer c
      WHERE (:keyword IS NULL OR :keyword = ''
             OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR c.phone LIKE CONCAT('%', :keyword, '%'))
      """)
  Page<Customer> search(@Param("keyword") String keyword, Pageable pageable);
}
