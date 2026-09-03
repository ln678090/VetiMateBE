package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CancelRequestReq {
  @NotBlank(message = "Reason is required")
  private String reason;
}
