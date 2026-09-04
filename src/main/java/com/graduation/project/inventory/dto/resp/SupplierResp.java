package com.graduation.project.inventory.dto.resp;

import java.util.UUID;

public record SupplierResp(UUID id, String name, String phone, String email, Boolean isActive) {}
