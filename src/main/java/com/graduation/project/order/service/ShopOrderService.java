package com.graduation.project.order.service;

import com.graduation.project.order.dto.req.UpdateOrderStatusRequest;
import com.graduation.project.order.dto.resp.ShopOrderListResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import java.util.UUID;

public interface ShopOrderService {

  ShopOrderListResp getAllOrdersForStaff(int page, int size, String status);

  ShopOrderResp getOrderById(UUID id);

  ShopOrderResp updateOrderStatus(UUID id, UpdateOrderStatusRequest request);

  ShopOrderResp approveCancel(UUID id);
  ShopOrderResp rejectCancel(UUID id);

  // Customer Methods
  ShopOrderResp createOrder(com.graduation.project.order.dto.req.CreateOrderRequest request, UUID userId);

  ShopOrderListResp getUserOrders(UUID userId, int page, int size);
  
  ShopOrderResp cancelOrder(UUID orderId, UUID userId, String reason);

  // POS (Point of Sale) - Bán hàng tại quầy
  ShopOrderResp createPosOrder(com.graduation.project.order.dto.req.CreatePosOrderRequest request, UUID staffId);
}
