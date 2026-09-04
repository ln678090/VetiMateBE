package com.graduation.project.inventory.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.inventory.dto.req.MedicineRequest;
import com.graduation.project.inventory.dto.resp.MedicineResp;
import com.graduation.project.inventory.service.MedicineService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/medicines")
@RequiredArgsConstructor
public class MedicineController {

  private final MedicineService medicineService;

  @GetMapping
  public ApiResp<List<MedicineResp>> getAll(
      @RequestParam(required = false, defaultValue = "false") Boolean all) {
    List<MedicineResp> data =
        Boolean.TRUE.equals(all)
            ? medicineService.getAllMedicines()
            : medicineService.getAllActiveMedicines();
    return ApiResp.<List<MedicineResp>>builder()
        .message("Lấy danh sách thuốc/vật tư thành công")
        .data(data)
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/{id}")
  public ApiResp<MedicineResp> getById(@PathVariable UUID id) {
    return ApiResp.<MedicineResp>builder()
        .message("Lấy thông tin thuốc/vật tư thành công")
        .data(medicineService.getById(id))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResp<MedicineResp> create(@Valid @RequestBody MedicineRequest request) {
    return ApiResp.<MedicineResp>builder()
        .message("Tạo thuốc/vật tư thành công")
        .data(medicineService.create(request))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}")
  public ApiResp<MedicineResp> update(
      @PathVariable UUID id, @Valid @RequestBody MedicineRequest request) {
    return ApiResp.<MedicineResp>builder()
        .message("Cập nhật thuốc/vật tư thành công")
        .data(medicineService.update(id, request))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}/toggle-active")
  public ApiResp<Void> toggleActive(@PathVariable UUID id) {
    medicineService.toggleActive(id);
    return ApiResp.<Void>builder()
        .message("Cập nhật trạng thái thuốc/vật tư thành công")
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/low-stock")
  public ApiResp<List<MedicineResp>> getLowStock() {
    return ApiResp.<List<MedicineResp>>builder()
        .message("Lấy danh sách thuốc/vật tư tồn kho thấp")
        .data(medicineService.getLowStockMedicines())
        .timestamp(Instant.now().toString())
        .build();
  }
}
