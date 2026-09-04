package com.graduation.project.inventory.dto.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MedicineRequest(
    @NotBlank(message = "Tên thuốc/vật tư không được để trống")
        @Size(max = 200, message = "Tên tối đa 200 ký tự")
        String name,
    @Size(max = 50, message = "SKU tối đa 50 ký tự") String sku,
    @NotBlank(message = "Đơn vị tính không được để trống")
        @Size(max = 30, message = "Đơn vị tính tối đa 30 ký tự")
        String unit,
    @NotNull(message = "Tồn tối thiểu không được để trống")
        @DecimalMin(value = "0", message = "Tồn tối thiểu >= 0")
        BigDecimal minStock,
    @NotNull(message = "Giá nhập không được để trống")
        @DecimalMin(value = "0", message = "Giá nhập >= 0")
        BigDecimal importPrice,
    @NotNull(message = "Giá bán không được để trống")
        @DecimalMin(value = "0", message = "Giá bán >= 0")
        BigDecimal sellPrice) {}
