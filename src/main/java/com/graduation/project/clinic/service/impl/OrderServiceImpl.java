package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.req.POSCheckoutRequest;
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
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.repository.StaffRepository;
import com.graduation.project.notification.service.NotificationService;
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
    private final StaffRepository staffRepository;
    private final NotificationService notificationService;

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
        Invoice invoice = Invoice.builder()
                .customer(customer)
                .type("SHOP")
                .status("DRAFT")
                .items(new java.util.ArrayList<>())
                .discountAmount(BigDecimal.ZERO)
                .build();
        
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
    @Transactional
    public OrderResponse posCheckout(UUID currentUserId, POSCheckoutRequest request) {
        // Find or create walk-in customer
        Customer customer = customerRepository.findByPhone("0000000000")
                .orElseGet(() -> {
                    Customer newCust = new Customer();
                    newCust.setFullName("Khách vãng lai");
                    newCust.setPhone("0000000000");
                    newCust.setAddress("Tại quầy");
                    return customerRepository.save(newCust);
                });

        // Build invoice
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setType("SHOP");
        invoice.setStatus("PAID"); // POS is usually paid immediately
        invoice.setPaymentMethod(request.getPaymentMethod());
        invoice.setPaidAt(java.time.Instant.now()); // Required by DB constraint when status is PAID
        invoice.setInvoiceCode("POS-" + System.currentTimeMillis());
        invoice.setNote(request.getNote());
        
        Staff staff = staffRepository.findByUserIdAndActiveTrue(currentUserId).orElse(null);
        if (staff != null) {
            invoice.setCreatedBy(staff.getId());
        }

        BigDecimal total = BigDecimal.ZERO;

        for (POSCheckoutRequest.CartItemReq itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            
            // Decrease stock quantity
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

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

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getPosHistory(java.time.Instant startDate, java.time.Instant endDate) {
        return invoiceRepository.findByTypeAndStatusAndPaidAtBetweenOrderByPaidAtDesc("SHOP", "PAID", startDate, endDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllShopOrders() {
        // Return only online shop orders (starting with ORD-)
        return invoiceRepository.findAll()
                .stream()
                .filter(inv -> "SHOP".equals(inv.getType()))
                .filter(inv -> inv.getInvoiceCode() != null && inv.getInvoiceCode().startsWith("ORD-"))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID id, String newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"SHOP".equals(invoice.getType())) {
            throw new RuntimeException("Only SHOP orders can be updated here");
        }

        invoice.setStatus(newStatus);
        invoice = invoiceRepository.save(invoice);
        
        if (invoice.getCustomer() != null && invoice.getCustomer().getUserId() != null) {
            String statusStr = switch (newStatus) {
                case "CONFIRMED" -> "đã được xác nhận";
                case "SHIPPING" -> "đang được giao";
                case "DELIVERED" -> "đã giao thành công";
                case "CANCELLED" -> "đã bị hủy";
                default -> "được cập nhật trạng thái";
            };
            notificationService.createNotification(
                    invoice.getCustomer().getUserId(),
                    "Cập nhật đơn hàng " + invoice.getInvoiceCode(),
                    "Đơn hàng của bạn " + statusStr + ".",
                    "/profile/orders?orderId=" + invoice.getId()
            );
        }
        
        return mapToResponse(invoice);
    }

    @Override
    @Transactional
    public OrderResponse cancelRequest(UUID id, UUID currentUserId, com.graduation.project.clinic.dto.req.CancelRequestReq req) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (invoice.getCustomer().getUserId() != null && !invoice.getCustomer().getUserId().equals(currentUserId)) {
            throw new RuntimeException("Access denied");
        }
        
        if ("DELIVERED".equals(invoice.getStatus()) || "CANCELLED".equals(invoice.getStatus())) {
            throw new RuntimeException("Cannot cancel an order that is already delivered or cancelled");
        }

        String note = invoice.getNote() != null ? invoice.getNote() : "";
        note = note + " | [CANCEL_REQUEST]: " + req.getReason();
        if (note.length() > 500) {
            note = note.substring(0, 497) + "...";
        }
        invoice.setNote(note);
        
        invoice = invoiceRepository.save(invoice);
        return mapToResponse(invoice);
    }

    @Override
    @Transactional
    public OrderResponse processCancelRequest(UUID id, com.graduation.project.clinic.dto.req.ProcessCancelReq req) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"SHOP".equals(invoice.getType())) {
            throw new RuntimeException("Only SHOP orders can be updated here");
        }

        if (req.getAccept()) {
            invoice.setStatus("CANCELLED");
        } else {
            // Reject cancellation, remove the tag from note
            String note = invoice.getNote();
            if (note != null && note.contains("| [CANCEL_REQUEST]:")) {
                int idx = note.indexOf("| [CANCEL_REQUEST]:");
                note = note.substring(0, idx).trim();
                invoice.setNote(note);
            }
        }

        invoice = invoiceRepository.save(invoice);
        
        if (invoice.getCustomer() != null && invoice.getCustomer().getUserId() != null) {
            String msg = req.getAccept() ? "Yêu cầu hủy đơn hàng của bạn đã được chấp nhận." : "Yêu cầu hủy đơn hàng của bạn đã bị từ chối.";
            notificationService.createNotification(
                    invoice.getCustomer().getUserId(),
                    "Phản hồi yêu cầu hủy đơn " + invoice.getInvoiceCode(),
                    msg,
                    "/profile/orders?orderId=" + invoice.getId()
            );
        }

        return mapToResponse(invoice);
    }

    private OrderResponse mapToResponse(Invoice invoice) {
        String feStatus = switch (invoice.getStatus()) {
            case "DRAFT" -> "PENDING";
            case "CONFIRMED" -> "CONFIRMED";
            case "SHIPPING" -> "SHIPPING";
            case "DELIVERED" -> "DELIVERED";
            case "PAID" -> "DELIVERED"; // POS orders usually PAID immediately, map to DELIVERED
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
                .updatedAt(invoice.getUpdatedAt())
                .paymentMethod(fePaymentMethod)
                .shippingAddress(invoice.getCustomer().getAddress())
                .note(invoice.getNote())
                .customerName(invoice.getCustomer().getFullName())
                .customerPhone(invoice.getCustomer().getPhone())
                .items(itemResponses)
                .build();
    }
}
