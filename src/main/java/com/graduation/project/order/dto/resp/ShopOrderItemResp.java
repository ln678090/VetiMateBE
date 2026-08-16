package com.graduation.project.order.dto.resp;

import java.math.BigDecimal;
import java.util.UUID;

public record ShopOrderItemResp(
    UUID id,
    UUID productId,
    String productName,
    String productImage,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal total) {}
