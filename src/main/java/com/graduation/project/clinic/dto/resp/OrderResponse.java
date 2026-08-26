package com.graduation.project.clinic.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String code;
    private String status; // Mapped for FE: 'PENDING', 'CONFIRMED', 'CANCELLED'
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private Instant createdAt;
    private String paymentMethod;
    private String shippingAddress;
    private String note;
    
    private List<OrderItemResponse> items;
}
