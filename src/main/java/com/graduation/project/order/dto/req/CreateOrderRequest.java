package com.graduation.project.order.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Recipient name is required") String recipientName,
    @NotBlank(message = "Recipient phone is required") String recipientPhone,
    @NotBlank(message = "Shipping address is required") String shippingAddress,
    String paymentMethod,
    String note,
    @NotEmpty(message = "Order must contain at least one item") 
    @Valid List<OrderItemRequest> items
) {}
