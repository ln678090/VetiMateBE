package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.resp.OrderItemResponse;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Invoice;
import com.graduation.project.clinic.entity.InvoiceItem;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.InvoiceRepository;
import com.graduation.project.clinic.service.OrderService;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse checkout(UUID currentUserId, CheckoutRequest request) {
        // Find or create customer
        Customer customer = customerRepository.findByUserId(currentUserId)
                .orElseGet(() -> {
                    Customer newCust = new Customer();
                    newCust.setUserId(currentUserId);
                    newCust.setFullName(request.getFullName());
                    newCust.setPhone(request.getPhone());
                    return newCust;
                });

        // Update address / note in customer (optional) or build a shipping address string
        String shippingAddress = request.getSpecificAddress() + ", " + request.getDistrict() + ", " + request.getCity();
        customer.setAddress(shippingAddress);
        customerRepository.save(customer);

        // Build invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setType("SHOP");
        invoice.setStatus("DRAFT");
        
        String paymentMethod = request.getPaymentMethod();
        if ("COD".equals(paymentMethod)) {
            paymentMethod = "CASH";
        }
        invoice.setPaymentMethod(paymentMethod);
        
        invoice.setInvoiceCode("ORD-" + System.currentTimeMillis());
        
        // Save shipping details in note since DB doesn't have a dedicated column
        String note = "Shipping Address: " + shippingAddress + " | Phone: " + request.getPhone() + " | Note: " + request.getNote();
        if (note.length() > 500) {
            note = note.substring(0, 497) + "...";
        }
        invoice.setNote(note);

        BigDecimal total = BigDecimal.ZERO;

        for (CheckoutRequest.CartItemReq itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setNameSnapshot(product.getName());
            item.setQuantity(new BigDecimal(itemReq.getQuantity()));
            item.setUnitPrice(product.getPrice());
            
            BigDecimal itemTotal = product.getPrice().multiply(item.getQuantity());
            item.setTotal(itemTotal);
            
            invoice.getItems().add(item);
            total = total.add(itemTotal);
        }

        invoice.setSubtotal(total);
        invoice.setTotalAmount(total);

        invoice = invoiceRepository.save(invoice);

        return mapToResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(UUID currentUserId) {
        return invoiceRepository.findByCustomer_UserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .filter(inv -> "SHOP".equals(inv.getType()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id, UUID currentUserId) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
                
        if (invoice.getCustomer().getUserId() != null && !invoice.getCustomer().getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied");
        }
        
        return mapToResponse(invoice);
    }

    private OrderResponse mapToResponse(Invoice invoice) {
        String feStatus = switch (invoice.getStatus()) {
            case "DRAFT" -> "PENDING";
            case "PAID" -> "CONFIRMED";
            case "CANCELLED" -> "CANCELLED";
            default -> "PENDING";
        };

        List<OrderItemResponse> itemResponses = invoice.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getNameSnapshot())
                        .productImage(item.getProduct() != null ? item.getProduct().getImageUrl() : null)
                        .price(item.getUnitPrice())
                        .quantity(item.getQuantity().intValue())
                        .build())
                .collect(Collectors.toList());

        String fePaymentMethod = invoice.getPaymentMethod();
        if ("CASH".equals(fePaymentMethod)) {
            fePaymentMethod = "COD";
        }

        return OrderResponse.builder()
                .id(invoice.getId())
                .code(invoice.getInvoiceCode())
                .status(feStatus)
                .totalAmount(invoice.getTotalAmount())
                .shippingFee(BigDecimal.ZERO)
                .finalAmount(invoice.getTotalAmount())
                .createdAt(invoice.getCreatedAt())
                .paymentMethod(fePaymentMethod)
                .shippingAddress(invoice.getCustomer().getAddress())
                .note(invoice.getNote())
                .items(itemResponses)
                .build();
    }
}
