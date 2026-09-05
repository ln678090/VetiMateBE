package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
//    @NotBlank(message = "Email không được để trống")
//    @Email(message = "Email không đúng định dạng")
//    String email,
//    @NotBlank(message = "Mã OTP không được để trống")
//    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP gồm 6 chữ số")
//    String otp,
//    @NotBlank(message = "Mật khẩu mới không được để trống")
//    @Pattern(
//        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*()_\\-]).{8,}$",
//        message = "Mật khẩu mới phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
//    )
//    String newPassword,
//    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
//    String confirmPassword
) {}
