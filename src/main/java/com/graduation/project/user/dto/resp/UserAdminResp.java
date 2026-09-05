package com.graduation.project.user.dto.resp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserAdminResp(
    UUID id,
    String username,
    String email,
    String fullName,
    String phone,
    Boolean enabled,
    OffsetDateTime createdAt,
    List<String> roles
) {}
