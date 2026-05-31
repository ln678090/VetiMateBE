package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Tên không được để trống") String fullName,
    @Email(message = "Email không hợp lệ") @NotBlank(message = "Email không được để trống")
        String email,
    @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
        String password,
    @NotBlank(message = "username không được để trống")
        @Size(min = 6, message = "username phải từ 6 ký tự trở lên")
        String username) {}
