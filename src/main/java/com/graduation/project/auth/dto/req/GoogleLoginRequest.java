package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
    @NotBlank(message = "Google Token không được để trống") String idToken) {}
