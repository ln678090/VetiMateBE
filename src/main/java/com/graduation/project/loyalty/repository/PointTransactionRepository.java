package com.graduation.project.loyalty.repository;

import com.graduation.project.loyalty.entity.PointTransaction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointTransactionRepository extends JpaRepository<PointTransaction, UUID> {
  List<PointTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
