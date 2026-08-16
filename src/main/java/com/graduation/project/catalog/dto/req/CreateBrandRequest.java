package com.graduation.project.catalog.dto.req;

import jakarta.validation.constraints.NotBlank;

public record CreateBrandRequest(
    @NotBlank(message = "Tên thương hiệu không được để trống") String name,
    String description,
    String logoUrl) {}
