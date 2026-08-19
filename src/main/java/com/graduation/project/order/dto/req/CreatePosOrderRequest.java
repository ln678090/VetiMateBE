package com.graduation.project.order.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for POS (Point of Sale) orders.
 * No shipping info required — customer buys in-store.
 */
public record CreatePosOrderRequest(
    String customerName,
    String customerPhone,
    String paymentMethod,
    String note,
    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    @Valid List<PosOrderItemRequest> items
) {}
