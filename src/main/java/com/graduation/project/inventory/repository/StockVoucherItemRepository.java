package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.StockVoucherItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockVoucherItemRepository extends JpaRepository<StockVoucherItem, UUID> {

  List<StockVoucherItem> findByVoucherIdOrderById(UUID voucherId);
}
