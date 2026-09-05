package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
//    @NotBlank(message = "Mật khẩu cũ không được để trống") String oldPassword,
//    @NotBlank(message = "Mật khẩu mới không được để trống")
//        @Pattern(
//            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()_\\-]).{8,}$",
//            message =
//                "Mật khẩu mới phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
//        String newPassword,
//    @NotBlank(message = "Xác nhận mật khẩu không được để trống") String confirmPassword
) {}
