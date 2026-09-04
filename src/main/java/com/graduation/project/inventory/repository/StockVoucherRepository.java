package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.StockVoucher;
import com.graduation.project.inventory.entity.VoucherStatus;
import com.graduation.project.inventory.entity.VoucherType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StockVoucherRepository
    extends JpaRepository<StockVoucher, UUID>, JpaSpecificationExecutor<StockVoucher> {

  Page<StockVoucher> findByTypeAndStatusOrderByCreatedAtDesc(
      VoucherType type, VoucherStatus status, Pageable pageable);

  Page<StockVoucher> findByTypeOrderByCreatedAtDesc(VoucherType type, Pageable pageable);

  Page<StockVoucher> findByStatusOrderByCreatedAtDesc(VoucherStatus status, Pageable pageable);

  Page<StockVoucher> findAllByOrderByCreatedAtDesc(Pageable pageable);

  long countByStatus(VoucherStatus status);
}
