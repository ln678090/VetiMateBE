package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessCancelReq {
  @NotNull(message = "Accept flag is required")
  private Boolean accept;
}
