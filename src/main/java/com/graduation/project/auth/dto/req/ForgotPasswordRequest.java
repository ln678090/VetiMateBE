package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
//    @NotBlank(message = "Email không được để trống")
//    @Email(message = "Email không đúng định dạng")
//    String email
) {}
