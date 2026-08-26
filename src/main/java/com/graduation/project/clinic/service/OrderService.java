package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderResponse;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse checkout(UUID currentUserId, CheckoutRequest request);
    List<OrderResponse> getMyOrders(UUID currentUserId);
    OrderResponse getOrderById(UUID id, UUID currentUserId);
}
