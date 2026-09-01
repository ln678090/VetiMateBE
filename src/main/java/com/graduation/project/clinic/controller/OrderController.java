package com.graduation.project.clinic.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(UUID.fromString(jwt.getSubject()), request));
    }

    @PostMapping("/pos-checkout")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
    public ResponseEntity<OrderResponse> posCheckout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.graduation.project.clinic.dto.req.POSCheckoutRequest request) {
        return ResponseEntity.ok(orderService.posCheckout(UUID.fromString(jwt.getSubject()), request));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getMyOrders(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
    public ResponseEntity<List<OrderResponse>> getAllShopOrders() {
        return ResponseEntity.ok(orderService.getAllShopOrders());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody com.graduation.project.clinic.dto.req.UpdateOrderStatusReq request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @GetMapping("/pos-history")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
    public ResponseEntity<List<OrderResponse>> getPosHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        java.time.Instant start = startDate != null && !startDate.isEmpty()
                ? java.time.LocalDate.parse(startDate.substring(0, 10)).atStartOfDay(zoneId).toInstant()
                : java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        
        java.time.Instant end = endDate != null && !endDate.isEmpty()
                ? java.time.LocalDate.parse(endDate.substring(0, 10)).plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)
                : java.time.Instant.now();
                
        return ResponseEntity.ok(orderService.getPosHistory(start, end));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(orderService.getOrderById(id, UUID.fromString(jwt.getSubject())));
    }

    @PostMapping("/{id}/cancel-request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> cancelRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.graduation.project.clinic.dto.req.CancelRequestReq request) {
        return ResponseEntity.ok(orderService.cancelRequest(id, UUID.fromString(jwt.getSubject()), request));
    }

    @PostMapping("/{id}/process-cancel-request")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SHOP_STAFF')")
    public ResponseEntity<OrderResponse> processCancelRequest(
            @PathVariable UUID id,
            @Valid @RequestBody com.graduation.project.clinic.dto.req.ProcessCancelReq request) {
        return ResponseEntity.ok(orderService.processCancelRequest(id, request));
    }
}


