package com.graduation.project.clinic.dto.resp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String code;
    private String status; // Mapped for FE: 'PENDING', 'CONFIRMED', 'CANCELLED'
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private String paymentMethod;
    private String shippingAddress;
    private String note;
    private String customerName;
    private String customerPhone;
    private Boolean isReviewed;
    
    private List<OrderItemResponse> items;
}
