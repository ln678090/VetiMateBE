package com.graduation.project.catalog.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandReq {
  @NotBlank(message = "Tên thương hiệu không được để trống")
  private String name;

  private String description;

  private String logoUrl;

  private Boolean isActive = true;
}
