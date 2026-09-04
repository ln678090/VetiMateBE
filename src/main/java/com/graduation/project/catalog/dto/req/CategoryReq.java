package com.graduation.project.catalog.dto.req;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.Data;

@Data
public class CategoryReq {
  @NotBlank(message = "Tên danh mục không được để trống")
  private String name;

  private String description;

  private UUID parentId;

  private Boolean isActive = true;
}
