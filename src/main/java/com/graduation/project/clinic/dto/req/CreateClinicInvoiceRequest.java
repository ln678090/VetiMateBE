package com.graduation.project.clinic.dto.req;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class CreateClinicInvoiceRequest {

  @NotNull(message = "Customer ID is required")
  private UUID customerId;

  private UUID petId;

  private String note;

  @NotNull(message = "Items are required")
  private List<ItemReq> items;

  @Data
  public static class ItemReq {
    private UUID serviceId;
    private UUID productId;
    private UUID medicineId;

    @NotNull private String name;

    @NotNull private BigDecimal quantity;

    @NotNull private BigDecimal unitPrice;
  }
}
