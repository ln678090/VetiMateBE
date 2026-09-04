package com.graduation.project.loyalty.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.loyalty.dto.CreateVoucherReq;
import com.graduation.project.loyalty.dto.VoucherDto;
import com.graduation.project.loyalty.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/management/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class VoucherManagementController {

    private final LoyaltyService loyaltyService;

    @GetMapping
    public ResponseEntity<ApiResp<List<VoucherDto>>> getAllVouchers() {
        return ResponseEntity.ok(ApiResp.<List<VoucherDto>>builder()
                .data(loyaltyService.getAllVouchers())
                .message("Success").build());
    }

    @PostMapping
    public ResponseEntity<ApiResp<VoucherDto>> createVoucher(
            @Valid @RequestBody CreateVoucherReq req) {
        return ResponseEntity.ok(ApiResp.<VoucherDto>builder()
                .data(loyaltyService.createVoucher(req))
                .message("Success").build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResp<VoucherDto>> updateVoucher(
            @PathVariable UUID id,
            @Valid @RequestBody CreateVoucherReq req) {
        return ResponseEntity.ok(ApiResp.<VoucherDto>builder()
                .data(loyaltyService.updateVoucher(id, req))
                .message("Success").build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResp<Void>> deleteVoucher(@PathVariable UUID id) {
        loyaltyService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResp.<Void>builder().message("Success").build());
    }
}
