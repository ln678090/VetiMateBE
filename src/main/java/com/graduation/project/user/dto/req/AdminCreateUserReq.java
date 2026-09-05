package com.graduation.project.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateUserReq(
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    String username,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu ít nhất 6 ký tự")
    String password,

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên quá dài")
    String fullName,

    @Email(message = "Email không đúng định dạng")
    String email,

    String phone,

    @NotBlank(message = "Vai trò không được để trống")
    String roleName
) {}
