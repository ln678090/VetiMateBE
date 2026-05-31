package com.graduation.project.product.dto.resp;

import java.util.List;

/** Wrapper cho paginated list - khớp với FE type ProductListResponse. */
public record ProductListResp(
    List<ProductResp> items, long total, int page, int size, int totalPages) {}
