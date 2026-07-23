package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.PetRequest;
import com.graduation.project.clinic.service.PetService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/pets")
@RequiredArgsConstructor
public class PetController {

  private final PetService petService;

  // POST /api/clinic/pets - Tạo pet
  @PostMapping
  public ResponseEntity<ApiResp<PetDto>> create(@Valid @RequestBody PetRequest request) {
    PetDto dto = petService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(
        ApiResp.<PetDto>builder().message("Tạo pet thành công").data(dto).build());
  }

  // PUT /api/clinic/pets/{id} - Sửa pet
  @PutMapping("/{id}")
  public ResponseEntity<ApiResp<PetDto>> update(@PathVariable UUID id,
      @Valid @RequestBody PetRequest request) {
    PetDto dto = petService.update(id, request);
    return ResponseEntity.ok(
        ApiResp.<PetDto>builder().message("Cập nhật pet thành công").data(dto).build());
  }

  // GET /api/clinic/pets/{id} - Chi tiết pet
  @GetMapping("/{id}")
  public ResponseEntity<ApiResp<PetDto>> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ApiResp.<PetDto>builder().message("OK").data(petService.getById(id)).build());
  }

  // GET /api/clinic/pets?customerId= - List pet theo chủ (paged)
  @GetMapping
  public ResponseEntity<ApiResp<Page<PetDto>>> getByCustomer(
      @RequestParam UUID customerId,
      @PageableDefault(size = 20) Pageable pageable) {
    Page<PetDto> page = petService.getByCustomer(customerId, pageable);
    return ResponseEntity.ok(
        ApiResp.<Page<PetDto>>builder().message("OK").data(page).build());
  }

  // DELETE /api/clinic/pets/{id} - Xóa pet
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResp<Void>> delete(@PathVariable UUID id) {
    petService.delete(id);
    return ResponseEntity.ok(
        ApiResp.<Void>builder().message("Xóa pet thành công").build());
  }
}
