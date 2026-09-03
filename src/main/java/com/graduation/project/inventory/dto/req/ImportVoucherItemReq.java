package com.graduation.project.inventory.dto.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportVoucherItemReq {
  private UUID productId;
  private UUID medicineId;
  private UUID supplierId;
  private String batchCode;

  @NotNull(message = "Số lượng không được để trống")
  @Min(value = 1, message = "Số lượng phải lớn hơn 0")
  private BigDecimal quantity;

  @NotNull(message = "Giá nhập không được để trống")
  @Min(value = 0, message = "Giá nhập không hợp lệ")
  private BigDecimal importPrice;

  private LocalDate expiryDate;
  private String note;
}
