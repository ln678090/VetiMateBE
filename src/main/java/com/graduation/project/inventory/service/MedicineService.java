package com.graduation.project.inventory.service;

import com.graduation.project.inventory.dto.req.MedicineRequest;
import com.graduation.project.inventory.dto.resp.MedicineResp;
import java.util.List;
import java.util.UUID;

public interface MedicineService {

  List<MedicineResp> getAllActiveMedicines();

  List<MedicineResp> getAllMedicines();

  MedicineResp getById(UUID id);

  MedicineResp create(MedicineRequest request);

  MedicineResp update(UUID id, MedicineRequest request);

  void toggleActive(UUID id);

  /** Thuốc/vật tư có tồn kho thấp hơn minStock */
  List<MedicineResp> getLowStockMedicines();
}
