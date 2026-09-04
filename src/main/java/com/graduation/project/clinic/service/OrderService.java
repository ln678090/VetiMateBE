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
    List<OrderResponse> getAllShopOrders();
    OrderResponse updateOrderStatus(UUID id, String newStatus);
    OrderResponse cancelRequest(UUID id, UUID currentUserId, com.graduation.project.clinic.dto.req.CancelRequestReq req);
    OrderResponse processCancelRequest(UUID id, com.graduation.project.clinic.dto.req.ProcessCancelReq req);
    OrderResponse reviewOrder(UUID id, UUID currentUserId, com.graduation.project.clinic.dto.req.ReviewOrderReq req);
}
