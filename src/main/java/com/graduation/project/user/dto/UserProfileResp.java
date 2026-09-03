package com.graduation.project.user.dto;

import lombok.Builder;

@Builder
public record UserProfileResp(
    String id,
    String fullName,
    String username,
    String email,
    String phone) {
}
