package com.graduation.project.clinic.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OrderResponse> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(orderService.checkout(userId, request));
    }

    @PostMapping("/pos-checkout")
    public ResponseEntity<OrderResponse> posCheckout(
            Authentication authentication,
            @Valid @RequestBody com.graduation.project.clinic.dto.req.POSCheckoutRequest request) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(orderService.posCheckout(userId, request));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }

    @GetMapping("/pos-history")
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
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = SecurityUtils.currentUserId(authentication);
        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }
}


