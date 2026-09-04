package com.graduation.project.loyalty.repository;

import com.graduation.project.loyalty.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, UUID> {
    List<UserVoucher> findByUserIdOrderByRedeemedAtDesc(UUID userId);
    
    boolean existsByUserIdAndVoucherId(UUID userId, UUID voucherId);
}
