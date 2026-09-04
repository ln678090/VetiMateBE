package com.graduation.project.product.dto.resp;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProductReviewResp {
    private UUID id;
    private String user;
    private String avatar;
    private Integer rating;
    private Instant createdAt;
    private String title;
    private String content;
    private Integer helpful;
}
