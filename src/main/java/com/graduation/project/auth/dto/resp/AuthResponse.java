package com.graduation.project.auth.dto.resp;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    String accessToken, String refreshToken, String tokenType // Sẽ luôn là "Bearer"
    ) {}
