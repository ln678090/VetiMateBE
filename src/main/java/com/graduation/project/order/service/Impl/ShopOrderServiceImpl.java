package com.graduation.project.order.service.Impl;

import com.graduation.project.order.dto.req.UpdateOrderStatusRequest;
import com.graduation.project.order.dto.resp.ShopOrderListResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import com.graduation.project.order.entity.ShopOrder;
import com.graduation.project.order.entity.ShopOrder.OrderStatus;
import com.graduation.project.order.mapper.ShopOrderMapper;
import com.graduation.project.order.repository.ShopOrderRepository;
import com.graduation.project.order.service.ShopOrderService;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopOrderServiceImpl implements ShopOrderService {

  private final ShopOrderRepository orderRepository;
  private final ShopOrderMapper orderMapper;
  private final com.graduation.project.user.repository.UserRepository userRepository;
  private final com.graduation.project.product.repository.ProductRepository productRepository;

  @Override
  public ShopOrderListResp getAllOrdersForStaff(int page, int size, String status) {
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
    Page<ShopOrder> result;

    if (status != null && !status.isBlank()) {
      try {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        result = orderRepository.findByStatusOrderByCreatedAtDesc(orderStatus, pageable);
      } catch (IllegalArgumentException e) {
        result = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
      }
    } else {
      result = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    return new ShopOrderListResp(
        orderMapper.toRespList(result.getContent()),
        result.getTotalElements(),
        result.getNumber(),
        result.getSize(),
        result.getTotalPages());
  }

  @Override
  public ShopOrderResp getOrderById(UUID id) {
    ShopOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng: " + id));
    return orderMapper.toResp(order);
  }

  @Transactional
  @Override
  public ShopOrderResp updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
    ShopOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng: " + id));
    
    OrderStatus oldStatus = order.getStatus();
    OrderStatus newStatus = request.status();
    
    if (newStatus == OrderStatus.CANCELLED) {
      throw new IllegalArgumentException("Nhân viên không được phép hủy đơn hàng. Chỉ khách hàng mới có thể hủy đơn.");
    }
    
    if (newStatus.ordinal() <= oldStatus.ordinal()) {
      throw new IllegalArgumentException("Không thể quay lại trạng thái trước đó.");
    }
    
    if (newStatus.ordinal() - oldStatus.ordinal() > 1 && oldStatus != OrderStatus.PENDING) { // Actually, let's just strictly enforce old+1 == new
      // Wait, if old is 0, new must be 1.
    }
    
    if (newStatus.ordinal() != oldStatus.ordinal() + 1) {
       throw new IllegalArgumentException("Phải cập nhật tuần tự từng trạng thái, không được nhảy cóc.");
    }

    if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
      // Deduct stock
      for (var item : order.getItems()) {
        var product = item.getProduct();
        if (product.getStockQuantity() < item.getQuantity()) {
          throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng trong kho (" + product.getStockQuantity() + " còn lại). Không thể xác nhận đơn.");
        }
        product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
        productRepository.save(product);
      }
    }

    order.setStatus(newStatus);
    
    return orderMapper.toResp(orderRepository.save(order));
  }

  @Transactional
  @Override
  public ShopOrderResp approveCancel(UUID id) {
    ShopOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng: " + id));
        
    if (!order.isCancellationRequested()) {
        throw new IllegalArgumentException("Đơn hàng này không có yêu cầu hủy");
    }
    
    // Nếu đơn hàng đã từng được xác nhận (bất kỳ trạng thái nào lớn hơn PENDING), tồn kho đã bị trừ
    if (order.getStatus().ordinal() > OrderStatus.PENDING.ordinal()) {
        for (var item : order.getItems()) {
            var product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }
    
    order.setStatus(OrderStatus.CANCELLED);
    order.setCancellationRequested(false);
    
    return orderMapper.toResp(orderRepository.save(order));
  }

  @Transactional
  @Override
  public ShopOrderResp rejectCancel(UUID id) {
    ShopOrder order = orderRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng: " + id));
        
    if (!order.isCancellationRequested()) {
        throw new IllegalArgumentException("Đơn hàng này không có yêu cầu hủy");
    }
    
    order.setCancellationRequested(false);
    // Keep the status as is
    
    return orderMapper.toResp(orderRepository.save(order));
  }

  @Transactional
  @Override
  public ShopOrderResp createOrder(com.graduation.project.order.dto.req.CreateOrderRequest request, UUID userId) {
    ShopOrder order = new ShopOrder();
    order.setOrderCode("ORD-" + System.currentTimeMillis());
    order.setUser(userRepository.getReferenceById(userId));
    order.setStatus(OrderStatus.PENDING);
    order.setRecipientName(request.recipientName());
    order.setRecipientPhone(request.recipientPhone());
    order.setShippingAddress(request.shippingAddress());
    order.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "COD");
    order.setNote(request.note());

    java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;

    for (com.graduation.project.order.dto.req.OrderItemRequest itemReq : request.items()) {
      com.graduation.project.order.entity.ShopOrderItem orderItem = new com.graduation.project.order.entity.ShopOrderItem();
      orderItem.setOrder(order);
      
      com.graduation.project.product.entity.Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm"));
          
      // Note: Stock deduction is deferred to when the order is CONFIRMED
      
      orderItem.setProduct(product);
      orderItem.setProductName(product.getName());
      orderItem.setProductImage(product.getImageUrl());
      orderItem.setQuantity(itemReq.quantity());
      orderItem.setUnitPrice(product.getPrice()); // Always use real DB price
      orderItem.setTotal(product.getPrice().multiply(java.math.BigDecimal.valueOf(itemReq.quantity())));
      
      subtotal = subtotal.add(orderItem.getTotal());
      order.getItems().add(orderItem);
    }

    order.setSubtotal(subtotal);
    // Fixed shipping fee logic
    java.math.BigDecimal shippingFee = subtotal.compareTo(java.math.BigDecimal.valueOf(500000)) >= 0 
        ? java.math.BigDecimal.ZERO 
        : java.math.BigDecimal.valueOf(30000);
    order.setShippingFee(shippingFee);
    order.setTotalAmount(subtotal.add(shippingFee));

    return orderMapper.toResp(orderRepository.save(order));
  }

  @Override
  public ShopOrderListResp getUserOrders(UUID userId, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
    Page<ShopOrder> result = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    
    return new ShopOrderListResp(
        orderMapper.toRespList(result.getContent()),
        result.getTotalElements(),
        result.getNumber(),
        result.getSize(),
        result.getTotalPages());
  }

  @Transactional
  @Override
  public ShopOrderResp cancelOrder(UUID orderId, UUID userId, String reason) {
    ShopOrder order = orderRepository.findById(orderId)
        .orElseThrow(() -> new NoSuchElementException("Không tìm thấy đơn hàng: " + orderId));
        
    if (!order.getUser().getId().equals(userId)) {
        throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền hủy đơn hàng này");
    }
    
    if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
        throw new IllegalArgumentException("Không thể hủy đơn hàng ở trạng thái này");
    }
    
    if (order.getStatus() == OrderStatus.PENDING) {
        // Hủy ngay lập tức
        order.setStatus(OrderStatus.CANCELLED);
    } else {
        // Gửi yêu cầu hủy
        order.setCancellationRequested(true);
        order.setCancellationReason(reason);
    }
    
    return orderMapper.toResp(orderRepository.save(order));
  }

  @Transactional
  @Override
  public ShopOrderResp createPosOrder(com.graduation.project.order.dto.req.CreatePosOrderRequest request, UUID staffId) {
    ShopOrder order = new ShopOrder();
    order.setOrderCode("POS-" + System.currentTimeMillis());
    // POS orders are linked to the staff who created them
    order.setUser(userRepository.getReferenceById(staffId));
    // POS orders are completed immediately
    order.setStatus(OrderStatus.COMPLETED);
    order.setRecipientName(request.customerName() != null ? request.customerName() : "Khách lẻ");
    order.setRecipientPhone(request.customerPhone() != null ? request.customerPhone() : "N/A");
    order.setShippingAddress("Mua tại quầy");
    order.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "CASH");
    order.setNote(request.note());
    // No shipping fee for POS
    order.setShippingFee(java.math.BigDecimal.ZERO);

    java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;

    for (com.graduation.project.order.dto.req.PosOrderItemRequest itemReq : request.items()) {
      com.graduation.project.product.entity.Product product = productRepository.findById(itemReq.productId())
          .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sản phẩm: " + itemReq.productId()));

      // Check stock
      if (product.getStockQuantity() < itemReq.quantity()) {
        throw new IllegalArgumentException(
            "Sản phẩm \"" + product.getName() + "\" không đủ tồn kho. Còn lại: " + product.getStockQuantity());
      }

      // Deduct stock immediately for POS
      product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
      productRepository.save(product);

      com.graduation.project.order.entity.ShopOrderItem orderItem = new com.graduation.project.order.entity.ShopOrderItem();
      orderItem.setOrder(order);
      orderItem.setProduct(product);
      orderItem.setProductName(product.getName());
      orderItem.setProductImage(product.getImageUrl());
      orderItem.setQuantity(itemReq.quantity());
      orderItem.setUnitPrice(product.getPrice());
      orderItem.setTotal(product.getPrice().multiply(java.math.BigDecimal.valueOf(itemReq.quantity())));

      subtotal = subtotal.add(orderItem.getTotal());
      order.getItems().add(orderItem);
    }

    order.setSubtotal(subtotal);
    order.setTotalAmount(subtotal); // No shipping fee

    return orderMapper.toResp(orderRepository.save(order));
  }
}
