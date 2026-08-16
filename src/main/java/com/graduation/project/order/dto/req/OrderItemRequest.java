package com.graduation.project.order.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemRequest(
    @NotNull(message = "Product ID is required") UUID productId,
    @Min(value = 1, message = "Quantity must be at least 1") int quantity,
    @NotNull(message = "Unit price is required") BigDecimal unitPrice
) {}
