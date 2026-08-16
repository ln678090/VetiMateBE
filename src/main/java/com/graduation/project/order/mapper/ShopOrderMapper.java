package com.graduation.project.order.mapper;

import com.graduation.project.order.dto.resp.ShopOrderItemResp;
import com.graduation.project.order.dto.resp.ShopOrderResp;
import com.graduation.project.order.entity.ShopOrder;
import com.graduation.project.order.entity.ShopOrderItem;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShopOrderMapper {

  @Mapping(target = "userId", source = "user.id")
  @Mapping(target = "userName", source = "user.fullName")
  ShopOrderResp toResp(ShopOrder order);

  List<ShopOrderResp> toRespList(List<ShopOrder> orders);

  @Mapping(target = "productId", source = "product.id")
  ShopOrderItemResp toItemResp(ShopOrderItem item);

  List<ShopOrderItemResp> toItemRespList(List<ShopOrderItem> items);
}
