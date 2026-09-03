package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CheckoutRequest {
  @NotBlank private String fullName;

  @NotBlank private String phone;

  @NotBlank private String city;

  @NotBlank private String district;

  @NotBlank private String specificAddress;

  private String note;

  @NotBlank private String paymentMethod; // 'COD', 'BANK_TRANSFER', 'VNPAY', 'MOMO'

  @NotEmpty private List<CartItemReq> items;

  @Data
  public static class CartItemReq {
    @NotNull private java.util.UUID productId;

    @NotNull private Integer quantity;
  }
}
