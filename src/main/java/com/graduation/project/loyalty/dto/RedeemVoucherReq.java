package com.graduation.project.loyalty.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class RedeemVoucherReq {
    @NotNull
    private UUID voucherId;
}
