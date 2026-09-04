package com.graduation.project.loyalty.controller;

import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.loyalty.dto.*;
import com.graduation.project.loyalty.service.LoyaltyService;
import org.springframework.security.oauth2.jwt.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/points")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResp<PointsResponse>> getMyPoints(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResp.<PointsResponse>builder()
                .data(loyaltyService.getMyPoints(UUID.fromString(jwt.getSubject())))
                .message("Success").build());
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResp<List<TransactionDto>>> getMyTransactions(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResp.<List<TransactionDto>>builder()
                .data(loyaltyService.getMyTransactions(UUID.fromString(jwt.getSubject())))
                .message("Success").build());
    }

    @GetMapping("/vouchers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResp<List<VoucherDto>>> getAvailableVouchers() {
        return ResponseEntity.ok(ApiResp.<List<VoucherDto>>builder()
                .data(loyaltyService.getAvailableVouchers())
                .message("Success").build());
    }

    @GetMapping("/my-vouchers")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResp<List<UserVoucherDto>>> getMyVouchers(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResp.<List<UserVoucherDto>>builder()
                .data(loyaltyService.getMyVouchers(UUID.fromString(jwt.getSubject())))
                .message("Success").build());
    }

    @PostMapping("/vouchers/redeem")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResp<UserVoucherDto>> redeemVoucher(
            @RequestBody RedeemVoucherReq req,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResp.<UserVoucherDto>builder()
                .data(loyaltyService.redeemVoucher(UUID.fromString(jwt.getSubject()), req.getVoucherId()))
                .message("Success").build());
    }
}
