package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusReq {
  @NotBlank(message = "Status is required")
  private String status;
}
