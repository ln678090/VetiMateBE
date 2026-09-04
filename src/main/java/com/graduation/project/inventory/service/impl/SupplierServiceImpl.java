package com.graduation.project.inventory.service.impl;

import com.graduation.project.inventory.dto.req.SupplierRequest;
import com.graduation.project.inventory.dto.resp.SupplierResp;
import com.graduation.project.inventory.entity.Supplier;
import com.graduation.project.inventory.mapper.InventoryMapper;
import com.graduation.project.inventory.repository.SupplierRepository;
import com.graduation.project.inventory.service.SupplierService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

  private final SupplierRepository supplierRepository;
  private final InventoryMapper inventoryMapper;

  @Override
  public List<SupplierResp> getAllActiveSuppliers() {
    return inventoryMapper.toSupplierRespList(
        supplierRepository.findByIsActiveTrueOrderByNameAsc());
  }

  @Override
  public List<SupplierResp> getAllSuppliers() {
    return inventoryMapper.toSupplierRespList(supplierRepository.findAll());
  }

  @Override
  public SupplierResp getById(UUID id) {
    return inventoryMapper.toSupplierResp(findOrThrow(id));
  }

  @Override
  @Transactional
  public SupplierResp create(SupplierRequest request) {
    Supplier supplier =
        Supplier.builder()
            .name(request.name())
            .phone(request.phone())
            .email(request.email())
            .build();
    return inventoryMapper.toSupplierResp(supplierRepository.save(supplier));
  }

  @Override
  @Transactional
  public SupplierResp update(UUID id, SupplierRequest request) {
    Supplier supplier = findOrThrow(id);
    supplier.setName(request.name());
    supplier.setPhone(request.phone());
    supplier.setEmail(request.email());
    return inventoryMapper.toSupplierResp(supplierRepository.save(supplier));
  }

  @Override
  @Transactional
  public void toggleActive(UUID id) {
    Supplier supplier = findOrThrow(id);
    supplier.setIsActive(!supplier.getIsActive());
    supplierRepository.save(supplier);
  }

  private Supplier findOrThrow(UUID id) {
    return supplierRepository
        .findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhà cung cấp: " + id));
  }
}
