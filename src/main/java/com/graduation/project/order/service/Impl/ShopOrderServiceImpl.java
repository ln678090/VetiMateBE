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
    order.setStatus(request.status());
    
    if (request.status() == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
      for (var item : order.getItems()) {
        var product = item.getProduct();
        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
        productRepository.save(product);
      }
    }
    
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
          
      if (product.getStockQuantity() < itemReq.quantity()) {
        throw new IllegalArgumentException("Sản phẩm " + product.getName() + " không đủ số lượng trong kho");
      }
      
      product.setStockQuantity(product.getStockQuantity() - itemReq.quantity());
      productRepository.save(product);
      
      orderItem.setProduct(product);
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
}
