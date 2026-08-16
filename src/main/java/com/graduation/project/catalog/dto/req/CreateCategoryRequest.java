package com.graduation.project.catalog.dto.req;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateCategoryRequest(
    @NotBlank(message = "Tên danh mục không được để trống") String name,
    String description,
    String icon,
    UUID parentId,
    Integer sortOrder) {}
