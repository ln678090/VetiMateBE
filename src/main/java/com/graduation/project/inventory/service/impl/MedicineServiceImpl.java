package com.graduation.project.inventory.service.impl;

import com.graduation.project.inventory.dto.req.MedicineRequest;
import com.graduation.project.inventory.dto.resp.MedicineResp;
import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.inventory.repository.MedicineRepository;
import com.graduation.project.inventory.service.MedicineService;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineServiceImpl implements MedicineService {

  private final MedicineRepository medicineRepository;

  @Override
  public List<MedicineResp> getAllActiveMedicines() {
    return medicineRepository.findByIsActiveTrueOrderByNameAsc().stream()
        .map(this::toRespWithStock)
        .toList();
  }

  @Override
  public List<MedicineResp> getAllMedicines() {
    return medicineRepository.findAll().stream().map(this::toRespWithStock).toList();
  }

  @Override
  public MedicineResp getById(UUID id) {
    return toRespWithStock(findOrThrow(id));
  }

  @Override
  @Transactional
  public MedicineResp create(MedicineRequest request) {
    if (request.sku() != null && medicineRepository.existsBySku(request.sku())) {
      throw new IllegalArgumentException("SKU đã tồn tại: " + request.sku());
    }
    Medicine medicine =
        Medicine.builder()
            .name(request.name())
            .sku(request.sku())
            .unit(request.unit())
            .minStock(request.minStock())
            .importPrice(request.importPrice())
            .sellPrice(request.sellPrice())
            .build();
    return toRespWithStock(medicineRepository.save(medicine));
  }

  @Override
  @Transactional
  public MedicineResp update(UUID id, MedicineRequest request) {
    Medicine medicine = findOrThrow(id);

    // Check SKU uniqueness if changed
    if (request.sku() != null
        && !request.sku().equals(medicine.getSku())
        && medicineRepository.existsBySku(request.sku())) {
      throw new IllegalArgumentException("SKU đã tồn tại: " + request.sku());
    }

    medicine.setName(request.name());
    medicine.setSku(request.sku());
    medicine.setUnit(request.unit());
    medicine.setMinStock(request.minStock());
    medicine.setImportPrice(request.importPrice());
    medicine.setSellPrice(request.sellPrice());
    return toRespWithStock(medicineRepository.save(medicine));
  }

  @Override
  @Transactional
  public void toggleActive(UUID id) {
    Medicine medicine = findOrThrow(id);
    medicine.setIsActive(!medicine.getIsActive());
    medicineRepository.save(medicine);
  }

  @Override
  public List<MedicineResp> getLowStockMedicines() {
    return medicineRepository.findLowStockMedicines().stream()
        .map(this::toRespWithStock)
        .toList();
  }

  // ===== helpers =====

  private Medicine findOrThrow(UUID id) {
    return medicineRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy thuốc/vật tư: " + id));
  }

  private MedicineResp toRespWithStock(Medicine m) {
    BigDecimal totalStock = medicineRepository.sumRemainingByMedicineId(m.getId());
    return new MedicineResp(
        m.getId(),
        m.getName(),
        m.getSku(),
        m.getUnit(),
        m.getMinStock(),
        m.getImportPrice(),
        m.getSellPrice(),
        m.getIsActive(),
        totalStock != null ? totalStock : BigDecimal.ZERO);
  }
}
