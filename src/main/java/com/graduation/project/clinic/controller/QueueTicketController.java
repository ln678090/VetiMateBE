package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.QueueTicketDto;
import com.graduation.project.clinic.dto.req.QueueStatusUpdateRequest;
import com.graduation.project.clinic.dto.req.QueueTicketRequest;
import com.graduation.project.clinic.entity.QueueType;
import com.graduation.project.clinic.service.QueueTicketService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/queue")
@RequiredArgsConstructor
public class QueueTicketController {

    private final QueueTicketService queueTicketService;

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN', 'SPA_STAFF')")
    public ResponseEntity<ApiResp<List<QueueTicketDto>>> getTodayQueue(
            @RequestParam QueueType type) {
        List<QueueTicketDto> tickets = queueTicketService.getTodayQueue(type);
        return ResponseEntity.ok(ApiResp.<List<QueueTicketDto>>builder()
                .message("OK")
                .data(tickets)
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN', 'SPA_STAFF')")
    public ResponseEntity<ApiResp<QueueTicketDto>> createTicket(
            @Valid @RequestBody QueueTicketRequest request) {
        QueueTicketDto ticket = queueTicketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResp.<QueueTicketDto>builder()
                .message("Tạo số thứ tự thành công")
                .data(ticket)
                .build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN', 'SPA_STAFF')")
    public ResponseEntity<ApiResp<QueueTicketDto>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody QueueStatusUpdateRequest request) {
        QueueTicketDto ticket = queueTicketService.updateStatus(id, request);
        return ResponseEntity.ok(ApiResp.<QueueTicketDto>builder()
                .message("Cập nhật trạng thái thành công")
                .data(ticket)
                .build());
    }
}
