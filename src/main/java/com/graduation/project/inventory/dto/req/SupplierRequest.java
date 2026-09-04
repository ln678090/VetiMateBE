package com.graduation.project.inventory.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupplierRequest(
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
        @Size(max = 200, message = "Tên NCC tối đa 200 ký tự")
        String name,
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự") String phone,
    @Email(message = "Email không hợp lệ")
        @Size(max = 150, message = "Email tối đa 150 ký tự")
        String email) {}
