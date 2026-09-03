package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CustomerRequest(

    /** Optional: link toi User neu khach co tai khoan. */
    UUID userId,
    @NotBlank(message = "Ho ten khong duoc de trong") @Size(max = 150) String fullName,
    @NotBlank(message = "So dien thoai khong duoc de trong")
        @Pattern(regexp = "^(0|\\+84)\\d{9,10}$", message = "So dien thoai khong hop le")
        String phone,
    @Email(message = "Email khong hop le") @Size(max = 150) String email,
    @Size(max = 255) String address,
    @Size(max = 500) String note) {}
