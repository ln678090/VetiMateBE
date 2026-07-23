package com.graduation.project.clinic.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.dto.req.CustomerRequest;
import com.graduation.project.clinic.service.CustomerService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/clinic/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerService customerService;

  @GetMapping("/me/customer")
  public ApiResp<CustomerDto> getMyCustomer(
      Authentication auth) {
    UUID userId = SecurityUtils.currentUserId(auth);
    CustomerDto dto = customerService.getOrCreateForCurrentUser(userId, null);
    return ApiResp.<CustomerDto>builder()
        .message("Lấy hồ sơ khách hàng thành công")
        .data(dto)
        .build();
  }

  // POST /api/clinic/customers - Tạo khách hàng
  @PostMapping
  public ResponseEntity<ApiResp<CustomerDto>> create(@Valid @RequestBody CustomerRequest request) {
    CustomerDto dto = customerService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResp.<CustomerDto>builder().message("Tạo khách hàng thành công").data(dto).build());
  }

  // PUT /api/clinic/customers/{id} - Sửa khách hàng
  @PutMapping("/{id}")
  public ResponseEntity<ApiResp<CustomerDto>> update(@PathVariable UUID id,
      @Valid @RequestBody CustomerRequest request) {
    CustomerDto dto = customerService.update(id, request);
    return ResponseEntity.ok(
        ApiResp.<CustomerDto>builder().message("Cập nhật khách hàng thành công").data(dto).build());
  }

  // GET /api/clinic/customers/{id} - Chi tiết khách hàng
  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<CustomerDto>> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResp.<CustomerDto>builder().message("OK").data(customerService.getById(id)).build());
  }

  // GET /api/clinic/customers?keyword= - Tìm/list khách hàng (paged)
  @GetMapping
  public ResponseEntity<ApiResp<Page<CustomerDto>>> search(
      @RequestParam(required = false) String keyword,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<CustomerDto> page = customerService.search(keyword, pageable);
    return ResponseEntity.ok(
        ApiResp.<Page<CustomerDto>>builder().message("OK").data(page).build());
  }

  // DELETE /api/clinic/customers/{id} - Xóa khách hàng
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResp<Void>> delete(@PathVariable UUID id) {
    customerService.delete(id);
    return ResponseEntity.ok(
        ApiResp.<Void>builder().message("Xóa khách hàng thành công").build());
  }
}
