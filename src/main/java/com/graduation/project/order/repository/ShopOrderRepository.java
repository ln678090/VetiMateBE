package com.graduation.project.order.repository;

import com.graduation.project.order.entity.ShopOrder;
import com.graduation.project.order.entity.ShopOrder.OrderStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopOrderRepository
    extends JpaRepository<ShopOrder, UUID>, JpaSpecificationExecutor<ShopOrder> {

  Optional<ShopOrder> findByOrderCode(String orderCode);

  Page<ShopOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

  Page<ShopOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<ShopOrder> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

  long countByStatus(OrderStatus status);
}
