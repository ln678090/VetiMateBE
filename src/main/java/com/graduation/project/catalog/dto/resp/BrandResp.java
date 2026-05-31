package com.graduation.project.catalog.dto.resp;

import java.util.UUID;

public record BrandResp(UUID id, String name, String slug, String description, String logoUrl) {}
