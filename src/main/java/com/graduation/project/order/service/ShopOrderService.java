package com.graduation.project.order.service;

import com.graduation.project.order.dto.req.UpdateOrderStatusRequest;
import com.graduation.project.order.dto.resp.ShopOrderListResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import java.util.UUID;

public interface ShopOrderService {

  ShopOrderListResp getAllOrdersForStaff(int page, int size, String status);

  ShopOrderResp getOrderById(UUID id);

  ShopOrderResp updateOrderStatus(UUID id, UpdateOrderStatusRequest request);

  // Customer Methods
  ShopOrderResp createOrder(com.graduation.project.order.dto.req.CreateOrderRequest request, UUID userId);

  ShopOrderListResp getUserOrders(UUID userId, int page, int size);
}
