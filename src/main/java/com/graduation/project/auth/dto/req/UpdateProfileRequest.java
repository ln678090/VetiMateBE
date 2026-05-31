package com.graduation.project.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
        String fullName,
    @Size(max = 255, message = "Tiểu sử không được vượt quá 255 ký tự") String bio,
    @Size(max = 100, message = "Vị trí không được vượt quá 100 ký tự") String location,
    @Size(max = 500, message = "URL trang web không được vượt quá 500 ký tự") String websiteUrl) {}
