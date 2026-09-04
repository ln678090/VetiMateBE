package com.graduation.project.clinic.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class ReviewOrderReq {
  @NotEmpty(message = "Reviews cannot be empty")
  @Valid
  private List<ReviewProductReq> reviews;
}
