package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CustomerRequest(
    @NotNull(message = "Tài khoản chủ pet là bắt buộc") UUID userId,
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự") String note) {}
