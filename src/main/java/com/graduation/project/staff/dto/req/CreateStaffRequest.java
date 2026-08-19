package com.graduation.project.staff.dto.req;

import com.graduation.project.staff.entity.StaffRoleType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateStaffRequest(

    UUID userId,

    @NotBlank(message = "Họ tên nhân viên không được để trống") @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự") String fullName,

    @Pattern(regexp = "^$|^[0-9+()\\-\\s]{8,20}$", message = "Số điện thoại không hợp lệ") String phone,

    @NotNull(message = "Vai trò nhân viên không được để trống") StaffRoleType roleType,

    @Size(max = 100, message = "Số chứng chỉ không được vượt quá 100 ký tự") String licenseNumber,

    @NotNull(message = "Lương cơ bản không được để trống") @PositiveOrZero(message = "Lương cơ bản không được âm") BigDecimal baseSalary,

    @NotNull(message = "Tỷ lệ hoa hồng không được để trống") @DecimalMin(value = "0.00", message = "Hoa hồng không được âm") @DecimalMax(value = "100.00", message = "Hoa hồng không được vượt quá 100%") BigDecimal commissionRate

) {
}
