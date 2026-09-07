package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.req.CancelRequestReq;
import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.req.POSCheckoutRequest;
import com.graduation.project.clinic.dto.req.ProcessCancelReq;
import com.graduation.project.clinic.dto.req.ReviewOrderReq;
import com.graduation.project.clinic.dto.req.ReviewProductReq;
import com.graduation.project.clinic.dto.resp.OrderItemResponse;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Invoice;
import com.graduation.project.clinic.entity.InvoiceItem;
import com.graduation.project.clinic.entity.InvoiceReview;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.InvoiceRepository;
import com.graduation.project.clinic.repository.InvoiceReviewRepository;
import com.graduation.project.clinic.service.OrderService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.loyalty.entity.DiscountType;
import com.graduation.project.loyalty.entity.UserVoucher;
import com.graduation.project.loyalty.entity.Voucher;
import com.graduation.project.loyalty.repository.UserVoucherRepository;
import com.graduation.project.loyalty.service.LoyaltyService;
import com.graduation.project.notification.service.NotificationService;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private static final int MAX_NOTE_LENGTH = 500;

  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final NotificationService notificationService;
  private final LoyaltyService loyaltyService;
  private final UserVoucherRepository userVoucherRepository;
  private final InvoiceReviewRepository invoiceReviewRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public OrderResponse checkout(UUID currentUserId, CheckoutRequest request) {
    User account = requireUser(currentUserId);

    Customer customer =
        customerRepository.findByUser_Id(currentUserId).orElseGet(() -> createCustomer(account));

    String shippingAddress = buildShippingAddress(request);

    Invoice invoice =
        Invoice.builder()
            .customer(customer)
            .type("SHOP")
            .status("DRAFT")
            .items(new ArrayList<>())
            .discountAmount(BigDecimal.ZERO)
            .build();

    invoice.setPaymentMethod(normalizePaymentMethod(request.getPaymentMethod()));

    invoice.setInvoiceCode("ORD-" + System.currentTimeMillis());

    invoice.setNote(buildOrderNote(shippingAddress, request.getPhone(), request.getNote()));

    BigDecimal subtotal = addOrderItems(invoice, request.getItems());

    invoice.setSubtotal(subtotal);

    BigDecimal discount = applyVoucher(invoice, currentUserId, request.getUserVoucherId(), subtotal);

    invoice.setDiscountAmount(discount);
    invoice.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));

    Invoice savedInvoice = invoiceRepository.save(invoice);

    notificationService.createNotification(
        null,
        "Đơn hàng mới",
        "Có đơn hàng mới: "
            + invoice.getInvoiceCode()
            + " với tổng tiền "
            + invoice.getTotalAmount().toString()
            + "đ",
        "/staff/shop/orders");

    return mapToResponse(savedInvoice);
  }

  /**
   * Customer hiện bắt buộc gắn với User. POS khách vãng lai cần thiết kế invoice snapshot riêng
   * trước khi bật lại.
   */
  @Override
  @Transactional
  public OrderResponse posCheckout(UUID currentUserId, POSCheckoutRequest request) {
    throw new UnsupportedOperationException("POS checkout chưa được hỗ trợ trong phạm vi hiện tại");
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getMyOrders(UUID currentUserId) {
    return invoiceRepository.findByCustomer_User_IdOrderByCreatedAtDesc(currentUserId).stream()
        .filter(invoice -> "SHOP".equals(invoice.getType()))
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(UUID invoiceId, UUID currentUserId) {
    Invoice invoice = requireInvoice(invoiceId);

    requireOrderOwner(invoice, currentUserId);

    return mapToResponse(invoice);
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getPosHistory(Instant startDate, Instant endDate) {
    return invoiceRepository
        .findByTypeAndStatusAndPaidAtBetweenOrderByPaidAtDesc("SHOP", "PAID", startDate, endDate)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getAllShopOrders() {
    return invoiceRepository.findAll().stream()
        .filter(invoice -> "SHOP".equals(invoice.getType()))
        .filter(
            invoice ->
                invoice.getInvoiceCode() != null && invoice.getInvoiceCode().startsWith("ORD-"))
        .sorted((first, second) -> second.getCreatedAt().compareTo(first.getCreatedAt()))
        .map(this::mapToResponse)
        .toList();
  }

  private void refundVoucherIfAny(Invoice invoice) {
    if (invoice.getUserVoucher() != null) {
      com.graduation.project.loyalty.entity.UserVoucher uv = invoice.getUserVoucher();
      uv.setIsUsed(false);
      uv.setUsedAt(null);
      userVoucherRepository.save(uv);
    }
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(UUID invoiceId, String newStatus, String cancelReason) {
    Invoice invoice = requireInvoice(invoiceId);

    requireShopInvoice(invoice);

    invoice.setStatus(newStatus);

    if ("CANCELLED".equals(newStatus) && cancelReason != null && !cancelReason.trim().isEmpty()) {
      String note = invoice.getNote() != null ? invoice.getNote() : "";
      note = note + " | [SHOP_CANCELLED]: " + cancelReason.trim();
      if (note.length() > 500) {
        note = note.substring(0, 497) + "...";
      }
      invoice.setNote(note);
    }

    if ("CANCELLED".equals(newStatus)) {
      refundVoucherIfAny(invoice);
    }

    Invoice savedInvoice = invoiceRepository.save(invoice);

    UUID ownerUserId = getCustomerUserId(savedInvoice);

    if ("DELIVERED".equals(newStatus) && ownerUserId != null) {
      loyaltyService.earnPoints(ownerUserId, savedInvoice.getTotalAmount(), savedInvoice.getId());
    }

    if (ownerUserId != null) {
      sendOrderStatusNotification(savedInvoice, ownerUserId, newStatus, cancelReason);
    }

    return mapToResponse(savedInvoice);
  }

  @Override
  @Transactional
  public OrderResponse cancelRequest(UUID invoiceId, UUID currentUserId, CancelRequestReq request) {
    Invoice invoice = requireInvoice(invoiceId);

    requireOrderOwner(invoice, currentUserId);

    if ("DELIVERED".equals(invoice.getStatus()) || "CANCELLED".equals(invoice.getStatus())) {
      throw new IllegalStateException("Không thể hủy đơn đã giao hoặc đã hủy");
    }

    String currentNote = invoice.getNote() == null ? "" : invoice.getNote();

    String newNote = currentNote + " | [CANCEL_REQUEST]: " + normalizeText(request.getReason());

    invoice.setNote(truncateNote(newNote));

    Invoice savedInvoice = invoiceRepository.save(invoice);

    return mapToResponse(savedInvoice);
  }

  @Override
  @Transactional
  public OrderResponse processCancelRequest(UUID invoiceId, ProcessCancelReq request) {
    Invoice invoice = requireInvoice(invoiceId);

    requireShopInvoice(invoice);

    if (Boolean.TRUE.equals(request.getAccept())) {
      invoice.setStatus("CANCELLED");
      refundVoucherIfAny(invoice);
    } else {
      removeCancellationRequest(invoice);
    }

    Invoice savedInvoice = invoiceRepository.save(invoice);

    UUID ownerUserId = getCustomerUserId(savedInvoice);

    if (ownerUserId != null) {
      String message =
          Boolean.TRUE.equals(request.getAccept())
              ? "Yêu cầu hủy đơn hàng của bạn đã được chấp nhận."
              : "Yêu cầu hủy đơn hàng của bạn đã bị từ chối.";

      notificationService.createNotification(
          ownerUserId,
          "Phản hồi yêu cầu hủy đơn " + savedInvoice.getInvoiceCode(),
          message,
          "/profile/orders?orderId=" + savedInvoice.getId());
    }

    return mapToResponse(savedInvoice);
  }

  @Override
  @Transactional
  public OrderResponse reviewOrder(UUID invoiceId, UUID currentUserId, ReviewOrderReq request) {
    Invoice invoice = requireInvoice(invoiceId);

    requireOrderOwner(invoice, currentUserId);

    if (!"DELIVERED".equals(invoice.getStatus())) {
      throw new IllegalStateException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
    }

    if (Boolean.TRUE.equals(invoice.getIsReviewed())) {
      throw new IllegalStateException("Đơn hàng này đã được đánh giá");
    }

    invoice.setIsReviewed(true);

    Invoice savedInvoice = invoiceRepository.save(invoice);

    for (ReviewProductReq reviewRequest : request.getReviews()) {
      saveProductReview(savedInvoice, reviewRequest);
    }

    loyaltyService.addPoints(
        currentUserId,
        50,
        "Đánh giá đơn hàng " + savedInvoice.getInvoiceCode(),
        savedInvoice.getId());

    return mapToResponse(savedInvoice);
  }

  private User requireUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
  }

  private Customer createCustomer(User account) {
    Customer customer = Customer.builder().user(account).build();

    return customerRepository.save(customer);
  }

  private Invoice requireInvoice(UUID invoiceId) {
    return invoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
  }

  private void requireShopInvoice(Invoice invoice) {
    if (!"SHOP".equals(invoice.getType())) {
      throw new IllegalArgumentException("Chỉ đơn hàng SHOP được phép xử lý");
    }
  }

  private void requireOrderOwner(Invoice invoice, UUID currentUserId) {
    UUID ownerUserId = getCustomerUserId(invoice);

    if (ownerUserId == null || !ownerUserId.equals(currentUserId)) {
      throw new SecurityException("Bạn không có quyền truy cập đơn hàng");
    }
  }

  private UUID getCustomerUserId(Invoice invoice) {
    if (invoice.getCustomer() == null || invoice.getCustomer().getUser() == null) {
      return null;
    }

    return invoice.getCustomer().getUser().getId();
  }

  private String buildShippingAddress(CheckoutRequest request) {
    return String.join(
        ", ",
        normalizeText(request.getSpecificAddress()),
        normalizeText(request.getDistrict()),
        normalizeText(request.getCity()));
  }

  private String buildOrderNote(String shippingAddress, String phone, String customerNote) {
    String note =
        "Shipping Address: "
            + shippingAddress
            + " | Phone: "
            + normalizeText(phone)
            + " | Note: "
            + normalizeText(customerNote);

    return truncateNote(note);
  }

  private String truncateNote(String value) {
    if (value.length() <= MAX_NOTE_LENGTH) {
      return value;
    }

    return value.substring(0, MAX_NOTE_LENGTH - 3) + "...";
  }

  private String normalizeText(String value) {
    return value == null ? "" : value.trim();
  }

  private String normalizePaymentMethod(String paymentMethod) {
    return "COD".equals(paymentMethod) ? "CASH" : paymentMethod;
  }

  private BigDecimal addOrderItems(Invoice invoice, List<CheckoutRequest.CartItemReq> requests) {
    BigDecimal subtotal = BigDecimal.ZERO;

    for (CheckoutRequest.CartItemReq itemRequest : requests) {
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

      InvoiceItem item = new InvoiceItem();
      item.setInvoice(invoice);
      item.setProduct(product);
      item.setNameSnapshot(product.getName());
      item.setQuantity(BigDecimal.valueOf(itemRequest.getQuantity()));
      item.setUnitPrice(product.getPrice());

      BigDecimal itemTotal = product.getPrice().multiply(item.getQuantity());

      item.setTotal(itemTotal);
      invoice.getItems().add(item);

      subtotal = subtotal.add(itemTotal);
    }

    return subtotal;
  }

  private BigDecimal applyVoucher(Invoice invoice, UUID currentUserId, UUID userVoucherId, BigDecimal subtotal) {
    if (userVoucherId == null) {
      return BigDecimal.ZERO;
    }

    UserVoucher userVoucher =
        userVoucherRepository
            .findById(userVoucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));

    if (Boolean.TRUE.equals(userVoucher.getIsUsed())) {
      throw new IllegalStateException("Voucher đã được sử dụng");
    }

    if (userVoucher.getUser() == null || !userVoucher.getUser().getId().equals(currentUserId)) {
      throw new SecurityException("Voucher không thuộc tài khoản hiện tại");
    }

    Voucher voucher = userVoucher.getVoucher();

    if (voucher.getMinOrderAmount() != null
        && subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
      throw new IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu");
    }

    BigDecimal discount;

    if (voucher.getDiscountType() == DiscountType.FIXED) {
      discount = voucher.getDiscountValue();
    } else {
      discount = subtotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));

      if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
        discount = voucher.getMaxDiscount();
      }
    }

    userVoucher.setIsUsed(true);
    userVoucher.setUsedAt(LocalDateTime.now());

    userVoucherRepository.save(userVoucher);
    invoice.setUserVoucher(userVoucher);

    return discount.min(subtotal);
  }

  private void sendOrderStatusNotification(Invoice invoice, UUID ownerUserId, String newStatus, String cancelReason) {
    String statusText =
        switch (newStatus) {
          case "CONFIRMED" -> "đã được xác nhận";
          case "SHIPPING" -> "đang được giao";
          case "DELIVERED" -> "đã giao thành công";
          case "CANCELLED" -> "đã bị hủy";
          default -> "được cập nhật trạng thái";
        };

    String message = "Đơn hàng của bạn " + statusText + ".";
    if ("CANCELLED".equals(newStatus) && cancelReason != null && !cancelReason.trim().isEmpty()) {
      message += " Lý do: " + cancelReason.trim();
    }

    notificationService.createNotification(
        ownerUserId,
        "Cập nhật đơn hàng " + invoice.getInvoiceCode(),
        message,
        "/profile/orders?orderId=" + invoice.getId());
  }

  private void removeCancellationRequest(Invoice invoice) {
    String note = invoice.getNote();

    if (note == null || !note.contains("| [CANCEL_REQUEST]:")) {
      return;
    }

    int markerIndex = note.indexOf("| [CANCEL_REQUEST]:");

    invoice.setNote(note.substring(0, markerIndex).trim());
  }

  private void saveProductReview(Invoice invoice, ReviewProductReq request) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm: " + request.getProductId()));

    InvoiceReview review =
        InvoiceReview.builder()
            .invoice(invoice)
            .customer(invoice.getCustomer())
            .product(product)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();

    invoiceReviewRepository.save(review);

    List<InvoiceReview> reviews =
        invoiceReviewRepository.findByProduct_SlugOrderByCreatedAtDesc(product.getSlug());

    int reviewCount = reviews.size();

    double averageRating =
        reviews.stream().mapToInt(InvoiceReview::getRating).average().orElse(5.0);

    product.setReviewCount(reviewCount);
    product.setRating(BigDecimal.valueOf(averageRating));

    productRepository.save(product);
  }

  private OrderResponse mapToResponse(Invoice invoice) {
    String frontendStatus = mapFrontendStatus(invoice.getStatus());

    List<OrderItemResponse> items =
        invoice.getItems().stream()
            .map(
                item ->
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() == null ? null : item.getProduct().getId())
                        .productName(item.getNameSnapshot())
                        .productImage(
                            item.getProduct() == null ? null : item.getProduct().getImageUrl())
                        .price(item.getUnitPrice())
                        .quantity(item.getQuantity().intValue())
                        .stockQuantity(
                            item.getProduct() != null ? item.getProduct().getStockQuantity() : 0)
                        .isActive(
                            item.getProduct() != null ? item.getProduct().getIsActive() : false)
                        .build())
            .toList();

    String paymentMethod =
        "CASH".equals(invoice.getPaymentMethod()) ? "COD" : invoice.getPaymentMethod();

    User account = invoice.getCustomer() == null ? null : invoice.getCustomer().getUser();

    return OrderResponse.builder()
        .id(invoice.getId())
        .code(invoice.getInvoiceCode())
        .status(frontendStatus)
        .totalAmount(invoice.getSubtotal())
        .discountAmount(invoice.getDiscountAmount())
        .shippingFee(BigDecimal.ZERO)
        .finalAmount(invoice.getTotalAmount())
        .createdAt(invoice.getCreatedAt())
        .updatedAt(invoice.getUpdatedAt())
        .paymentMethod(paymentMethod)
        .shippingAddress(account == null ? null : account.getAddress())
        .note(invoice.getNote())
        .customerName(account == null ? null : account.getFullName())
        .customerPhone(account == null ? null : account.getPhone())
        .isReviewed(invoice.getIsReviewed())
        .items(items)
        .build();
  }

  private String mapFrontendStatus(String status) {
    return switch (status) {
      case "DRAFT" -> "PENDING";
      case "CONFIRMED" -> "CONFIRMED";
      case "SHIPPING" -> "SHIPPING";
      case "DELIVERED", "PAID" -> "DELIVERED";
      case "CANCELLED" -> "CANCELLED";
      default -> "PENDING";
    };
  }
}
