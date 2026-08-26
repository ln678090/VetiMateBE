package com.graduation.project.inventory.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImportVoucherReq {
  private String note;

  @NotEmpty(message = "Danh sách sản phẩm nhập không được để trống")
  @Valid
  private List<ImportVoucherItemReq> items;
}
