package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.req.POSCheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponse checkout(UUID currentUserId, CheckoutRequest request);
    OrderResponse posCheckout(UUID currentUserId, POSCheckoutRequest request);
    List<OrderResponse> getMyOrders(UUID currentUserId);
    OrderResponse getOrderById(UUID id, UUID currentUserId);
    List<OrderResponse> getPosHistory(Instant startDate, Instant endDate);
}
