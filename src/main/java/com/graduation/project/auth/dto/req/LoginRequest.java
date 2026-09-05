package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
//    @NotBlank(message = "Email không được để trống") String email,
//    @NotBlank(message = "Mật khẩu không được để trống") String password
) {}
