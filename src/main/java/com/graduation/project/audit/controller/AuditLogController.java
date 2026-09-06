package com.graduation.project.audit.controller;

import com.graduation.project.audit.dto.AuditLogResponse;
import com.graduation.project.audit.entity.AuditAction;
import com.graduation.project.audit.service.AuditLogQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

  private final AuditLogQueryService auditLogQueryService;

  @GetMapping
  public Page<AuditLogResponse> search(
      @RequestParam(required = false) String actor,
      @RequestParam(required = false) UUID createdBy,
      @RequestParam(required = false) String module,
      @RequestParam(required = false) AuditAction action,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("Thời gian bắt đầu không được sau thời gian kết thúc");
    }

    return auditLogQueryService.search(actor, createdBy, module, action, from, to, page, size);
  }
}
