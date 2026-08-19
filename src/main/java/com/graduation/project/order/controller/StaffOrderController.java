package com.graduation.project.order.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.order.dto.req.CreatePosOrderRequest;
import com.graduation.project.order.dto.req.UpdateOrderStatusRequest;
import com.graduation.project.order.dto.resp.ShopOrderListResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import com.graduation.project.order.service.ShopOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SHOP_STAFF', 'MANAGER')")
public class StaffOrderController {

  private final ShopOrderService orderService;

  @GetMapping
  public ResponseEntity<ApiResp<ShopOrderListResp>> getAllOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String status) {

    ShopOrderListResp resp = orderService.getAllOrdersForStaff(page, size, status);
    return ResponseEntity.ok(ApiResp.<ShopOrderListResp>builder()
        .message("Lấy danh sách đơn hàng thành công")
        .data(resp)
        .build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<ShopOrderResp>> getOrderById(@PathVariable UUID id) {
    ShopOrderResp resp = orderService.getOrderById(id);
    return ResponseEntity.ok(ApiResp.<ShopOrderResp>builder()
        .message("Lấy chi tiết đơn hàng thành công")
        .data(resp)
        .build());
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResp<ShopOrderResp>> updateOrderStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOrderStatusRequest request) {
    ShopOrderResp resp = orderService.updateOrderStatus(id, request);
    return ResponseEntity.ok(ApiResp.<ShopOrderResp>builder()
        .message("Cập nhật trạng thái đơn hàng thành công")
        .data(resp)
        .build());
  }

  @PostMapping("/{id}/cancel/approve")
  public ResponseEntity<ApiResp<ShopOrderResp>> approveCancel(@PathVariable UUID id) {
    ShopOrderResp resp = orderService.approveCancel(id);
    return ResponseEntity.ok(ApiResp.<ShopOrderResp>builder()
        .message("Đã chấp nhận yêu cầu hủy đơn hàng")
        .data(resp)
        .build());
  }

  @PostMapping("/{id}/cancel/reject")
  public ResponseEntity<ApiResp<ShopOrderResp>> rejectCancel(@PathVariable UUID id) {
    ShopOrderResp resp = orderService.rejectCancel(id);
    return ResponseEntity.ok(ApiResp.<ShopOrderResp>builder()
        .message("Đã từ chối yêu cầu hủy đơn hàng")
        .data(resp)
        .build());
  }

  @PostMapping("/pos")
  public ResponseEntity<ApiResp<ShopOrderResp>> createPosOrder(
      @Valid @RequestBody CreatePosOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ShopOrderResp resp = orderService.createPosOrder(request, userDetails.id());
    return ResponseEntity.ok(ApiResp.<ShopOrderResp>builder()
        .message("Thanh toán tại quầy thành công")
        .data(resp)
        .build());
  }
}

