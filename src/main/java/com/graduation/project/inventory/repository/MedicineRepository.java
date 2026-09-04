package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.Medicine;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicineRepository
    extends JpaRepository<Medicine, UUID>, JpaSpecificationExecutor<Medicine> {

  List<Medicine> findByIsActiveTrueOrderByNameAsc();

  Optional<Medicine> findByIdAndIsActiveTrue(UUID id);

  List<Medicine> findAllByIdInAndIsActiveTrue(Collection<UUID> ids);

  Optional<Medicine> findBySku(String sku);

  boolean existsBySku(String sku);

  /** Thuốc/vật tư có tồn kho dưới mức tối thiểu */
  @Query(
      """
      SELECT m FROM Medicine m
      WHERE m.isActive = true
        AND m.minStock > 0
        AND (SELECT COALESCE(SUM(sb.remainingQty), 0)
             FROM StockBatch sb
             WHERE sb.medicine = m
               AND sb.remainingQty > 0) < m.minStock
      ORDER BY m.name
      """)
  List<Medicine> findLowStockMedicines();

  /** Đếm tổng tồn kho cho 1 medicine */
  @Query(
      """
      SELECT COALESCE(SUM(sb.remainingQty), 0)
      FROM StockBatch sb
      WHERE sb.medicine.id = :medicineId
        AND sb.remainingQty > 0
      """)
  java.math.BigDecimal sumRemainingByMedicineId(@Param("medicineId") UUID medicineId);
}
