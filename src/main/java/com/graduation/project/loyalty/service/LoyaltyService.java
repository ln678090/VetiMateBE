package com.graduation.project.loyalty.service;

import com.graduation.project.loyalty.dto.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LoyaltyService {
    // For Users
    PointsResponse getMyPoints(UUID userId);
    List<TransactionDto> getMyTransactions(UUID userId);
    List<VoucherDto> getAvailableVouchers();
    List<UserVoucherDto> getMyVouchers(UUID userId);
    void earnPoints(UUID userId, BigDecimal orderAmount, UUID orderId);
    void addPoints(UUID userId, int points, String description, UUID orderId);
    UserVoucherDto redeemVoucher(UUID userId, UUID voucherId);
    
    // For Managers
    List<VoucherDto> getAllVouchers();
    VoucherDto createVoucher(CreateVoucherReq req);
    VoucherDto updateVoucher(UUID id, CreateVoucherReq req);
    void deleteVoucher(UUID id);
}
