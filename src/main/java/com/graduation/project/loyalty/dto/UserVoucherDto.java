package com.graduation.project.loyalty.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVoucherDto {
  private UUID id;
  private VoucherDto voucher;
  private LocalDateTime redeemedAt;
  private LocalDateTime usedAt;
  private Boolean isUsed;
}
