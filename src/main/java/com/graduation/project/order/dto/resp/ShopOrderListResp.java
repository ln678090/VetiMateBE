package com.graduation.project.order.dto.resp;

import java.util.List;

public record ShopOrderListResp(
    List<ShopOrderResp> items,
    long total,
    int page,
    int size,
    int totalPages) {}
