package com.graduation.project.order.dto.req;

import com.graduation.project.order.entity.ShopOrder.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Trạng thái không được để trống") OrderStatus status) {}
