package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
//    @NotBlank(message = "Tên không được để trống") String fullName,
//    @Email(message = "Email không hợp lệ") @NotBlank(message = "Email không được để trống")
//        String email,
//    @NotBlank(message = "Số điện thoại không được để trống")
//        @Pattern(
//            regexp = "^(0|\\+84)(3|5|7|8|9)[0-9]{8}$",
//            message = "Số điện thoại không hợp lệ (VD: 0987654321)")
//        String phone,
//    @NotBlank(message = "Mật khẩu không được để trống")
//        @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
//        String password,
//    @NotBlank(message = "username không được để trống")
//        @Size(min = 6, message = "username phải từ 6 ký tự trở lên")
//        String username
        ) {}
