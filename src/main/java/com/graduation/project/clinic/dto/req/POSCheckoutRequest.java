package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class POSCheckoutRequest {

    @NotBlank
    private String paymentMethod; // 'CASH', 'BANK_TRANSFER', 'VNPAY', 'MOMO'

    private String note;

    @NotEmpty
    private List<CartItemReq> items;

    @Data
    public static class CartItemReq {
        @NotNull
        private java.util.UUID productId;

        @NotNull
        private Integer quantity;
    }
}
