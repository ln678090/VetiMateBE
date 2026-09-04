package com.graduation.project.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateProfileRequest(
    @NotBlank(message = "Tên không được để trống") String fullName,
    @NotBlank(message = "Username không được để trống") String username,
    @Pattern(regexp = "^(?:\\+84|0)(?:3|5|7|8|9)\\d{8}$", message = "Số điện thoại không hợp lệ")
        String phone) {}
