package com.graduation.project.loyalty.service.impl;

import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.loyalty.dto.*;
import com.graduation.project.loyalty.entity.*;
import com.graduation.project.loyalty.repository.*;
import com.graduation.project.loyalty.service.LoyaltyService;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final VoucherRepository voucherRepository;
    private final UserLoyaltyPointsRepository pointsRepository;
    private final PointTransactionRepository transactionRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PointsResponse getMyPoints(UUID userId) {
        UserLoyaltyPoints points = pointsRepository.findById(userId)
                .orElse(UserLoyaltyPoints.builder().totalPoints(0).availablePoints(0).build());
        return PointsResponse.builder()
                .totalPoints(points.getTotalPoints())
                .availablePoints(points.getAvailablePoints())
                .totalSpending(points.getTotalSpending())
                .tier(points.getTier())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDto> getMyTransactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToTransactionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDto> getAvailableVouchers() {
        return voucherRepository.findAvailableVouchers(LocalDateTime.now()).stream()
                .map(this::mapToVoucherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucherDto> getMyVouchers(UUID userId) {
        return userVoucherRepository.findByUserIdOrderByRedeemedAtDesc(userId).stream()
                .map(this::mapToUserVoucherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void earnPoints(UUID userId, BigDecimal orderAmount, UUID orderId) {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) return;
        
        // 20,000 VND = 10 points => 1 VND = 10/20000 = 1/2000 points
        int earnedPoints = orderAmount.divide(new BigDecimal("2000"), RoundingMode.DOWN).intValue();
        
        if (earnedPoints > 0) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            UserLoyaltyPoints points = pointsRepository.findById(userId)
                    .orElseGet(() -> {
                        UserLoyaltyPoints newPoints = UserLoyaltyPoints.builder()
                                .user(user)
                                .userId(userId)
                                .totalPoints(0)
                                .availablePoints(0)
                                .build();
                        return pointsRepository.save(newPoints);
                    });
            
            points.setTotalPoints(points.getTotalPoints() + earnedPoints);
            points.setAvailablePoints(points.getAvailablePoints() + earnedPoints);
            points.setTotalSpending(points.getTotalSpending().add(orderAmount));
            points.setTier(CustomerTier.calculateTier(points.getTotalSpending()));
            pointsRepository.save(points);
            
            PointTransaction transaction = PointTransaction.builder()
                    .user(user)
                    .points(earnedPoints)
                    .type(TransactionType.EARN)
                    .description("Earned points from online order")
                    .orderId(orderId)
                    .build();
            transactionRepository.save(transaction);
        }
    }

    @Override
    @Transactional
    public void addPoints(UUID userId, int pointsAmount, String description, UUID orderId) {
        if (pointsAmount <= 0) return;
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserLoyaltyPoints points = pointsRepository.findById(userId)
                .orElseGet(() -> {
                    UserLoyaltyPoints newPoints = UserLoyaltyPoints.builder()
                            .user(user)
                            .userId(userId)
                            .totalPoints(0)
                            .availablePoints(0)
                            .build();
                    return pointsRepository.save(newPoints);
                });
        
        points.setTotalPoints(points.getTotalPoints() + pointsAmount);
        points.setAvailablePoints(points.getAvailablePoints() + pointsAmount);
        pointsRepository.save(points);
        
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .points(pointsAmount)
                .type(TransactionType.EARN)
                .description(description)
                .orderId(orderId)
                .build();
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public UserVoucherDto redeemVoucher(UUID userId, UUID voucherId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
                
        if (!voucher.getIsActive()) {
            throw new IllegalArgumentException("Voucher is not active");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
            throw new IllegalArgumentException("Voucher is not yet valid");
        }
        if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
            throw new IllegalArgumentException("Voucher is expired");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Voucher usage limit reached");
        }
        
        // Check if user already redeemed it (if we want to limit 1 per user, wait, let's just allow multiple if they have points, unless specified. I will not limit for now, or maybe I should?)
        // Let's not limit for now, just check points.
        
        UserLoyaltyPoints points = pointsRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not enough points"));
                
        if (points.getAvailablePoints() < voucher.getPointsRequired()) {
            throw new IllegalArgumentException("Not enough points to redeem this voucher");
        }
        
        if (voucher.getRequiredTier() != null) {
            if (voucher.getRequiredTier() != CustomerTier.MEMBER && points.getTier() != voucher.getRequiredTier()) {
                throw new IllegalArgumentException("Voucher is only available for " + voucher.getRequiredTier() + " tier");
            }
        }
        
        // Deduct points
        points.setAvailablePoints(points.getAvailablePoints() - voucher.getPointsRequired());
        pointsRepository.save(points);
        
        // Update voucher count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        
        // Create transaction
        PointTransaction transaction = PointTransaction.builder()
                .user(user)
                .points(voucher.getPointsRequired())
                .type(TransactionType.REDEEM)
                .description("Redeemed voucher " + voucher.getCode())
                .voucher(voucher)
                .build();
        transactionRepository.save(transaction);
        
        // Create UserVoucher
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .build();
        userVoucherRepository.save(userVoucher);
        
        return mapToUserVoucherDto(userVoucher);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDto> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(this::mapToVoucherDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VoucherDto createVoucher(CreateVoucherReq req) {
        Voucher voucher = Voucher.builder()
                .code(req.getCode().toUpperCase())
                .description(req.getDescription())
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .minOrderAmount(req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscount(req.getMaxDiscount())
                .pointsRequired(req.getPointsRequired())
                .usageLimit(req.getUsageLimit())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .requiredTier(req.getRequiredTier())
                .build();
        return mapToVoucherDto(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherDto updateVoucher(UUID id, CreateVoucherReq req) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));
                
        voucher.setCode(req.getCode().toUpperCase());
        voucher.setDescription(req.getDescription());
        voucher.setDiscountType(req.getDiscountType());
        voucher.setDiscountValue(req.getDiscountValue());
        if (req.getMinOrderAmount() != null) voucher.setMinOrderAmount(req.getMinOrderAmount());
        voucher.setMaxDiscount(req.getMaxDiscount());
        voucher.setPointsRequired(req.getPointsRequired());
        voucher.setUsageLimit(req.getUsageLimit());
        voucher.setStartDate(req.getStartDate());
        voucher.setEndDate(req.getEndDate());
        if (req.getIsActive() != null) voucher.setIsActive(req.getIsActive());
        voucher.setRequiredTier(req.getRequiredTier());
        
        return mapToVoucherDto(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deleteVoucher(UUID id) {
        voucherRepository.deleteById(id);
    }
    
    private VoucherDto mapToVoucherDto(Voucher voucher) {
        if (voucher == null) return null;
        return VoucherDto.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .description(voucher.getDescription())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderAmount(voucher.getMinOrderAmount())
                .maxDiscount(voucher.getMaxDiscount())
                .pointsRequired(voucher.getPointsRequired())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .isActive(voucher.getIsActive())
                .requiredTier(voucher.getRequiredTier())
                .createdAt(voucher.getCreatedAt())
                .build();
    }
    
    private TransactionDto mapToTransactionDto(PointTransaction t) {
        if (t == null) return null;
        return TransactionDto.builder()
                .id(t.getId())
                .points(t.getPoints())
                .type(t.getType())
                .description(t.getDescription())
                .orderId(t.getOrderId())
                .voucher(mapToVoucherDto(t.getVoucher()))
                .createdAt(t.getCreatedAt())
                .build();
    }
    
    private UserVoucherDto mapToUserVoucherDto(UserVoucher uv) {
        if (uv == null) return null;
        return UserVoucherDto.builder()
                .id(uv.getId())
                .voucher(mapToVoucherDto(uv.getVoucher()))
                .redeemedAt(uv.getRedeemedAt())
                .usedAt(uv.getUsedAt())
                .isUsed(uv.getIsUsed())
                .build();
    }
}
