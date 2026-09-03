package com.graduation.project.user.dto.resp;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UserProfileResp(
    UUID id,
    String fullName,
    String username,
    String email,
    String phone
) {}
