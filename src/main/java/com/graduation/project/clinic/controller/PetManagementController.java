package com.graduation.project.clinic.controller;

import com.graduation.project.clinic.dto.PetManagementSummary;
import com.graduation.project.clinic.dto.req.ManagementPetRequest;
import com.graduation.project.clinic.entity.PetSpecies;
import com.graduation.project.clinic.service.PetManagementService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clinic/management/pets")
@RequiredArgsConstructor
public class PetManagementController {

  private static final String READ_AUTHORITIES =
      "hasAnyAuthority("
          + "'ROLE_ADMIN',"
          + "'ROLE_MANAGER',"
          + "'ROLE_RECEPTIONIST',"
          + "'ROLE_DOCTOR'"
          + ")";

  private static final String WRITE_AUTHORITIES =
      "hasAnyAuthority(" + "'ROLE_ADMIN'," + "'ROLE_RECEPTIONIST'" + ")";

  private final PetManagementService petManagementService;

  @GetMapping
  @PreAuthorize(READ_AUTHORITIES)
  public ApiResp<Page<PetManagementSummary>> search(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) PetSpecies species,
      @RequestParam(defaultValue = "false") Boolean deleted,
      @RequestParam(required = false) UUID customerId,
      Pageable pageable) {
    return ApiResp.<Page<PetManagementSummary>>builder()
        .message("Lấy danh sách thú cưng thành công")
        .data(petManagementService.search(keyword, species, deleted, customerId, pageable))
        .build();
  }

  @GetMapping("/{petId}")
  @PreAuthorize(READ_AUTHORITIES)
  public ApiResp<PetManagementSummary> getById(@PathVariable UUID petId) {
    return ApiResp.<PetManagementSummary>builder()
        .message("Lấy thông tin thú cưng thành công")
        .data(petManagementService.getById(petId))
        .build();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize(WRITE_AUTHORITIES)
  public ApiResp<PetManagementSummary> create(@Valid @RequestBody ManagementPetRequest request) {
    return ApiResp.<PetManagementSummary>builder()
        .message("Tạo hồ sơ thú cưng thành công")
        .data(petManagementService.create(request))
        .build();
  }

  @PutMapping("/{petId}")
  @PreAuthorize(WRITE_AUTHORITIES)
  public ApiResp<PetManagementSummary> update(
      @PathVariable UUID petId, @Valid @RequestBody ManagementPetRequest request) {
    return ApiResp.<PetManagementSummary>builder()
        .message("Cập nhật thú cưng thành công")
        .data(petManagementService.update(petId, request))
        .build();
  }

  @DeleteMapping("/{petId}")
  @PreAuthorize(WRITE_AUTHORITIES)
  public ApiResp<Void> softDelete(@PathVariable UUID petId) {
    petManagementService.softDelete(petId);

    return ApiResp.<Void>builder().message("Xóa thú cưng thành công").data(null).build();
  }

  @PostMapping("/{petId}/restore")
  @PreAuthorize(WRITE_AUTHORITIES)
  public ApiResp<PetManagementSummary> restore(@PathVariable UUID petId) {
    return ApiResp.<PetManagementSummary>builder()
        .message("Khôi phục thú cưng thành công")
        .data(petManagementService.restore(petId))
        .build();
  }
}
