package com.graduation.project.clinic.customer.controller;

import com.graduation.project.clinic.customer.dto.StaffCustomerFilter;
import com.graduation.project.clinic.customer.dto.StaffCustomerSummary;
import com.graduation.project.clinic.customer.service.StaffCustomerQueryService;
import com.graduation.project.common.resp.ApiResp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/staff/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority(" +
    "'ROLE_RECEPTIONIST'," +
    "'ROLE_ADMIN'," +
    "'ROLE_MANAGER'" +
    ")")
public class StaffCustomerController {

  private final StaffCustomerQueryService staffCustomerQueryService;

  @GetMapping
  public ApiResp<Page<StaffCustomerSummary>> search(
      @RequestParam(defaultValue = "") String keyword,

      @RequestParam(defaultValue = "ALL") StaffCustomerFilter filter,

      Pageable pageable) {
    Page<StaffCustomerSummary> customers = staffCustomerQueryService.search(
        keyword,
        filter,
        pageable);

    return ApiResp
        .<Page<StaffCustomerSummary>>builder()
        .message(
            "Lấy danh sách khách hàng thành công")
        .data(customers)
        .build();
  }
}
