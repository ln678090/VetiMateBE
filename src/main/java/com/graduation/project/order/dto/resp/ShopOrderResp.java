package com.graduation.project.order.dto.resp;

import com.graduation.project.order.entity.ShopOrder.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ShopOrderResp(
    UUID id,
    String orderCode,
    UUID userId,
    String userName,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal shippingFee,
    BigDecimal totalAmount,
    String paymentMethod,
    String recipientName,
    String recipientPhone,
    String shippingAddress,
    String note,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    boolean cancellationRequested,
    String cancellationReason,
    List<ShopOrderItemResp> items) {}
