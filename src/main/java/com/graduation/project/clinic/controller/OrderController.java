package com.graduation.project.clinic.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(userDetails.id(), request));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(orderService.getMyOrders(userDetails.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.id()));
    }
}


