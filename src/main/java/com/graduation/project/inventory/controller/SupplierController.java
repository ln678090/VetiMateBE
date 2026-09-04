package com.graduation.project.inventory.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.inventory.dto.req.SupplierRequest;
import com.graduation.project.inventory.dto.resp.SupplierResp;
import com.graduation.project.inventory.service.SupplierService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/inventory/suppliers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAREHOUSE')")
public class SupplierController {

  private final SupplierService supplierService;

  @GetMapping
  public ApiResp<List<SupplierResp>> getAll(
      @RequestParam(required = false, defaultValue = "false") Boolean all) {
    List<SupplierResp> data =
        Boolean.TRUE.equals(all)
            ? supplierService.getAllSuppliers()
            : supplierService.getAllActiveSuppliers();
    return ApiResp.<List<SupplierResp>>builder()
        .message("Lấy danh sách nhà cung cấp thành công")
        .data(data)
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping("/{id}")
  public ApiResp<SupplierResp> getById(@PathVariable UUID id) {
    return ApiResp.<SupplierResp>builder()
        .message("Lấy thông tin nhà cung cấp thành công")
        .data(supplierService.getById(id))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResp<SupplierResp> create(@Valid @RequestBody SupplierRequest request) {
    return ApiResp.<SupplierResp>builder()
        .message("Tạo nhà cung cấp thành công")
        .data(supplierService.create(request))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}")
  public ApiResp<SupplierResp> update(
      @PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
    return ApiResp.<SupplierResp>builder()
        .message("Cập nhật nhà cung cấp thành công")
        .data(supplierService.update(id, request))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}/toggle-active")
  public ApiResp<Void> toggleActive(@PathVariable UUID id) {
    supplierService.toggleActive(id);
    return ApiResp.<Void>builder()
        .message("Cập nhật trạng thái nhà cung cấp thành công")
        .timestamp(Instant.now().toString())
        .build();
  }
}
