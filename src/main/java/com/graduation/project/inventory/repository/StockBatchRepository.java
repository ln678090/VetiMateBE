package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.StockBatch;
import com.graduation.project.inventory.entity.WarehouseLocation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockBatchRepository extends JpaRepository<StockBatch, UUID> {

  /** Lô hàng còn tồn của 1 medicine — sắp xếp FEFO (First Expiry First Out) theo kho */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.medicine.id = :medicineId
        AND sb.warehouse = :warehouse
        AND sb.remainingQty > 0
      ORDER BY sb.expiryDate ASC NULLS LAST, sb.receivedAt ASC
      """)
  List<StockBatch> findAvailableBatchesByMedicineAndWarehouseFefo(
      @Param("medicineId") UUID medicineId, @Param("warehouse") WarehouseLocation warehouse);

  /** Lô hàng còn tồn của 1 medicine — mặc định STORAGE */
  default List<StockBatch> findAvailableBatchesByMedicineFefo(UUID medicineId) {
    return findAvailableBatchesByMedicineAndWarehouseFefo(medicineId, WarehouseLocation.STORAGE);
  }

  /** Lô hàng còn tồn của 1 product — FEFO theo kho */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.product.id = :productId
        AND sb.warehouse = :warehouse
        AND sb.remainingQty > 0
      ORDER BY sb.expiryDate ASC NULLS LAST, sb.receivedAt ASC
      """)
  List<StockBatch> findAvailableBatchesByProductAndWarehouseFefo(
      @Param("productId") UUID productId, @Param("warehouse") WarehouseLocation warehouse);

  /** Lô hàng còn tồn của 1 product — mặc định STORAGE */
  default List<StockBatch> findAvailableBatchesByProductFefo(UUID productId) {
    return findAvailableBatchesByProductAndWarehouseFefo(productId, WarehouseLocation.STORAGE);
  }

  /** Tính tổng tồn kho hiện tại của 1 product */
  @Query(
      """
      SELECT COALESCE(SUM(sb.remainingQty), 0) FROM StockBatch sb
      WHERE sb.product.id = :productId
      """)
  java.math.BigDecimal sumRemainingQtyByProductId(@Param("productId") UUID productId);

  /** Tất cả lô cận date theo kho */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.remainingQty > 0
        AND sb.warehouse = :warehouse
        AND sb.expiryDate IS NOT NULL
        AND sb.expiryDate <= :thresholdDate
        AND sb.expiryDate >= CURRENT_DATE
      ORDER BY sb.expiryDate ASC
      """)
  List<StockBatch> findNearExpiryBatchesByWarehouse(
      @Param("thresholdDate") LocalDate thresholdDate,
      @Param("warehouse") WarehouseLocation warehouse);

  default List<StockBatch> findNearExpiryBatches(LocalDate thresholdDate) {
    return findNearExpiryBatchesByWarehouse(thresholdDate, WarehouseLocation.STORAGE);
  }

  /** Tất cả lô đã hết hạn nhưng vẫn còn tồn theo kho */
  @Query(
      """
      SELECT sb FROM StockBatch sb
      WHERE sb.remainingQty > 0
        AND sb.warehouse = :warehouse
        AND sb.expiryDate IS NOT NULL
        AND sb.expiryDate < CURRENT_DATE
      ORDER BY sb.expiryDate ASC
      """)
  List<StockBatch> findExpiredBatchesByWarehouse(@Param("warehouse") WarehouseLocation warehouse);

  default List<StockBatch> findExpiredBatches() {
    return findExpiredBatchesByWarehouse(WarehouseLocation.STORAGE);
  }

  /** Tất cả lô theo kho */
  List<StockBatch> findByWarehouseOrderByReceivedAtDesc(WarehouseLocation warehouse);

  /** Tất cả lô của 1 medicine theo kho */
  List<StockBatch> findByMedicineIdAndWarehouseOrderByReceivedAtDesc(
      UUID medicineId, WarehouseLocation warehouse);

  /** Tất cả lô của 1 medicine (kể cả đã hết) */
  List<StockBatch> findByMedicineIdOrderByReceivedAtDesc(UUID medicineId);

  /** Tất cả lô của 1 product (kể cả đã hết) */
  List<StockBatch> findByProductIdOrderByReceivedAtDesc(UUID productId);

  /** Tìm lô theo medicine, batchCode và kho */
  Optional<StockBatch> findByMedicineIdAndBatchCodeAndWarehouse(
      UUID medicineId, String batchCode, WarehouseLocation warehouse);

  /** Tìm lô theo product, batchCode và kho */
  Optional<StockBatch> findByProductIdAndBatchCodeAndWarehouse(
      UUID productId, String batchCode, WarehouseLocation warehouse);
}
