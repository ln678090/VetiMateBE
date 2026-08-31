package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.StockVoucher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockVoucherRepository extends JpaRepository<StockVoucher, UUID> {}
