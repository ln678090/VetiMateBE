package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.Invoice;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
  List<Invoice> findByCustomer_UserIdOrderByCreatedAtDesc(UUID userId);

  @EntityGraph(attributePaths = {"customer", "customer.user", "items", "items.product"})
  List<Invoice> findByCustomer_User_IdOrderByCreatedAtDesc(UUID userId);

  List<Invoice> findByTypeAndStatusAndPaidAtBetweenOrderByPaidAtDesc(
      String type, String status, Instant startDate, Instant endDate);
}
