package com.graduation.project.product.dto.resp;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

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
