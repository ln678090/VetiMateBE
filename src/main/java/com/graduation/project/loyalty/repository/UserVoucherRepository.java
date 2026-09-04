package com.graduation.project.loyalty.repository;

import com.graduation.project.loyalty.entity.UserVoucher;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {
  List<UserVoucher> findByUserIdOrderByRedeemedAtDesc(UUID userId);

  boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);
}
