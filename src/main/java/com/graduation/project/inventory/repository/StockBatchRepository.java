package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.StockBatch;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, UUID> {

  /** Lô hàng còn tồn của 1 medicine — sắp xếp FEFO (First Expiry First Out) */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.medicine.id = :medicineId
        AND sb.remainingQty > 0
      ORDER BY sb.expiryDate ASC NULLS LAST, sb.receivedAt ASC
      """)
  List<StockBatch> findAvailableBatchesByMedicineFefo(@Param("medicineId") UUID medicineId);

  /** Lô hàng còn tồn của 1 product — FEFO */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.product.id = :productId
        AND sb.remainingQty > 0
      ORDER BY sb.expiryDate ASC NULLS LAST, sb.receivedAt ASC
      """)
  List<StockBatch> findAvailableBatchesByProductFefo(@Param("productId") UUID productId);

  /** Tính tổng tồn kho hiện tại của 1 product */
  @Query(
      """
      SELECT COALESCE(SUM(sb.remainingQty), 0) FROM StockBatch sb
      WHERE sb.product.id = :productId
      """)
  java.math.BigDecimal sumRemainingQtyByProductId(@Param("productId") UUID productId);

  /** Tất cả lô cận date (sắp hết hạn trước ngày chỉ định) */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.remainingQty > 0
        AND sb.expiryDate IS NOT NULL
        AND sb.expiryDate <= :thresholdDate
        AND sb.expiryDate >= CURRENT_DATE
      ORDER BY sb.expiryDate ASC
      """)
  List<StockBatch> findNearExpiryBatches(@Param("thresholdDate") LocalDate thresholdDate);

  /** Tất cả lô đã hết hạn nhưng vẫn còn tồn */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.remainingQty > 0
        AND sb.expiryDate IS NOT NULL
        AND sb.expiryDate < CURRENT_DATE
      ORDER BY sb.expiryDate ASC
      """)
  List<StockBatch> findExpiredBatches();

  /** Tất cả lô của 1 medicine (kể cả đã hết) */
  List<StockBatch> findByMedicineIdOrderByReceivedAtDesc(UUID medicineId);

  /** Tất cả lô của 1 product (kể cả đã hết) */
  List<StockBatch> findByProductIdOrderByReceivedAtDesc(UUID productId);
}
