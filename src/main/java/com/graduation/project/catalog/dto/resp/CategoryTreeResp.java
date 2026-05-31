package com.graduation.project.catalog.dto.resp;

import java.util.List;
import java.util.UUID;

public record CategoryTreeResp(
    UUID id,
    String name,
    String slug,
    String description,
    String icon,
    Integer sortOrder,
    List<CategoryTreeResp> children) {}
