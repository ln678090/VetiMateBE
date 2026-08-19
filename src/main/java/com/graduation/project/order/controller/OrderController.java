package com.graduation.project.order.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.order.dto.req.CreateOrderRequest;
import com.graduation.project.order.dto.resp.ShopOrderListResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import com.graduation.project.order.service.ShopOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final ShopOrderService shopOrderService;

  @PostMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ShopOrderResp> createOrder(
      @Valid @RequestBody CreateOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(shopOrderService.createOrder(request, userDetails.id()));
  }

  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ShopOrderListResp> getMyOrders(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(shopOrderService.getUserOrders(userDetails.id(), page, size));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ShopOrderResp> getOrderDetails(
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    // In a real app we should verify the order belongs to the user
    return ResponseEntity.ok(shopOrderService.getOrderById(id));
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ShopOrderResp> cancelOrder(
      @PathVariable UUID id,
      @RequestBody(required = false) java.util.Map<String, String> requestBody,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    String reason = requestBody != null ? requestBody.get("reason") : null;
    return ResponseEntity.ok(shopOrderService.cancelOrder(id, userDetails.id(), reason));
  }
}
