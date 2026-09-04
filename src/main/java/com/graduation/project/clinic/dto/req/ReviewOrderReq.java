package com.graduation.project.clinic.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ReviewOrderReq {
    @NotEmpty(message = "Reviews cannot be empty")
    @Valid
    private List<ReviewProductReq> reviews;
}
