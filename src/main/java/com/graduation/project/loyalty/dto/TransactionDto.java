package com.graduation.project.loyalty.dto;

import com.graduation.project.loyalty.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransactionDto {
    private UUID id;
    private Integer points;
    private TransactionType type;
    private String description;
    private UUID orderId;
    private VoucherDto voucher;
    private LocalDateTime createdAt;
}
