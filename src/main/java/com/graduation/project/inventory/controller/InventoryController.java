package com.graduation.project.inventory.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.inventory.dto.req.CreateImportVoucherReq;
import com.graduation.project.inventory.dto.resp.StockVoucherResp;
import com.graduation.project.inventory.service.InventoryService;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAREHOUSE')")
public class InventoryController {

  private final InventoryService inventoryService;

  @PostMapping("/import")
  public ApiResp<StockVoucherResp> createImportVoucher(
      @Valid @RequestBody CreateImportVoucherReq req,
      @AuthenticationPrincipal Jwt jwt) {
    // Hack: Normally user.getStaff().getId() would be used. Assuming Staff ID = User ID for testing if no staff found.
    // In real app, we should get the staff linked to the user.
    UUID staffId = UUID.fromString(jwt.getSubject()); 
    return ApiResp.<StockVoucherResp>builder()
        .message("Tạo phiếu nhập kho thành công")
        .data(inventoryService.createImportVoucher(req, staffId))
        .timestamp(Instant.now().toString())
        .build();
  }

  @PutMapping("/{id}/approve")
  public ApiResp<StockVoucherResp> approveVoucher(
      @PathVariable UUID id,
      @AuthenticationPrincipal Jwt jwt) {
    UUID staffId = UUID.fromString(jwt.getSubject()); 
    return ApiResp.<StockVoucherResp>builder()
        .message("Duyệt phiếu thành công")
        .data(inventoryService.approveVoucher(id, staffId))
        .timestamp(Instant.now().toString())
        .build();
  }

  @GetMapping
  public ApiResp<List<StockVoucherResp>> getAllVouchers() {
    return ApiResp.<List<StockVoucherResp>>builder()
        .message("Lấy danh sách phiếu thành công")
        .data(inventoryService.getAllVouchers())
        .timestamp(Instant.now().toString())
        .build();
  }
}
