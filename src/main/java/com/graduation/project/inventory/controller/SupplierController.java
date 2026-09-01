package com.graduation.project.inventory.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.inventory.entity.Supplier;
import com.graduation.project.inventory.service.InventoryService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAREHOUSE')")
public class SupplierController {

  private final InventoryService inventoryService;

  @GetMapping
  public ApiResp<List<Supplier>> getAllSuppliers() {
    return ApiResp.<List<Supplier>>builder()
        .message("Lấy danh sách nhà cung cấp thành công")
        .data(inventoryService.getAllSuppliers())
        .timestamp(Instant.now().toString())
        .build();
  }
}
