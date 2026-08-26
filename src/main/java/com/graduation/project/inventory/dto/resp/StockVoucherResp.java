package com.graduation.project.inventory.dto.resp;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockVoucherResp {
  private UUID id;
  private String type;
  private String status;
  private String createdBy;
  private String approvedBy;
  private String note;
  private String createdAt;
  private String approvedAt;
}
