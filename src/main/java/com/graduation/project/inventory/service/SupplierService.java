package com.graduation.project.inventory.service;

import com.graduation.project.inventory.dto.req.SupplierRequest;
import com.graduation.project.inventory.dto.resp.SupplierResp;
import java.util.List;
import java.util.UUID;

public interface SupplierService {

  List<SupplierResp> getAllActiveSuppliers();

  List<SupplierResp> getAllSuppliers();

  SupplierResp getById(UUID id);

  SupplierResp create(SupplierRequest request);

  SupplierResp update(UUID id, SupplierRequest request);

  void toggleActive(UUID id);
}
