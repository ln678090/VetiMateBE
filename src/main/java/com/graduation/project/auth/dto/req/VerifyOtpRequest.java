package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
//    @NotBlank(message = "Email không được để trống")
//    @Email(message = "Email không đúng định dạng")
//    String email,
//    @NotBlank(message = "Mã OTP không được để trống")
//    @Pattern(regexp = "^\\d{6}$", message = "Mã OTP gồm 6 chữ số")
//    String otp
) {}
