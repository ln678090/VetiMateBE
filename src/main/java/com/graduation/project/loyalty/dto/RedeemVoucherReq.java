package com.graduation.project.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class RedeemVoucherReq {
  @NotNull private UUID voucherId;
}
