package com.graduation.project.loyalty.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserVoucherDto {
    private UUID id;
    private VoucherDto voucher;
    private LocalDateTime redeemedAt;
    private LocalDateTime usedAt;
    private Boolean isUsed;
}
