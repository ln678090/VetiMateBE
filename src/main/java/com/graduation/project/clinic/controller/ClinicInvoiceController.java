package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.ClinicInvoiceDto;
import com.graduation.project.clinic.dto.req.CreateClinicInvoiceRequest;
import com.graduation.project.clinic.dto.req.PayClinicInvoiceRequest;
import com.graduation.project.clinic.service.ClinicInvoiceService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/invoices")
@RequiredArgsConstructor
public class ClinicInvoiceController {

    private final ClinicInvoiceService clinicInvoiceService;

    @GetMapping
    public ResponseEntity<ApiResp<List<ClinicInvoiceDto>>> getAllInvoices() {
        return ResponseEntity.ok(ApiResp.<List<ClinicInvoiceDto>>builder()
                .message("Lấy danh sách hóa đơn thành công")
                .data(clinicInvoiceService.getAllInvoices())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResp<ClinicInvoiceDto>> getInvoiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResp.<ClinicInvoiceDto>builder()
                .message("Lấy chi tiết hóa đơn thành công")
                .data(clinicInvoiceService.getInvoiceById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResp<ClinicInvoiceDto>> createInvoice(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateClinicInvoiceRequest request) {
        return ResponseEntity.ok(ApiResp.<ClinicInvoiceDto>builder()
                .message("Tạo hóa đơn thành công")
                .data(clinicInvoiceService.createInvoice(request, UUID.fromString(jwt.getSubject())))
                .build());
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<ApiResp<ClinicInvoiceDto>> payInvoice(
            @PathVariable UUID id,
            @Valid @RequestBody PayClinicInvoiceRequest request) {
        return ResponseEntity.ok(ApiResp.<ClinicInvoiceDto>builder()
                .message("Thanh toán hóa đơn thành công")
                .data(clinicInvoiceService.payInvoice(id, request))
                .build());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResp<ClinicInvoiceDto>> cancelInvoice(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResp.<ClinicInvoiceDto>builder()
                .message("Hủy hóa đơn thành công")
                .data(clinicInvoiceService.cancelInvoice(id))
                .build());
    }
}
