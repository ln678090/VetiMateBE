package com.graduation.project.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "Tên không được để trống")
        @Size(max = 100, message = "Tên tối đa 100 ký tự")
        String fullName,
    @NotBlank(message = "Username không được để trống")
        @Size(min = 3, max = 50, message = "Username từ 3-50 ký tự")
        String username,
    @Pattern(
            regexp = "^$|^(\\+84|0)(3|5|7|8|9)\\d{8}$",
            message = "Số điện thoại Việt Nam không hợp lệ")
        String phone) {}
