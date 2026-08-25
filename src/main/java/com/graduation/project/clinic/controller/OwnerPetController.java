package com.graduation.project.clinic.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.dto.PetDto;
import com.graduation.project.clinic.dto.req.OwnerPetRequest;
import com.graduation.project.clinic.service.OwnerPetService;
import com.graduation.project.common.resp.ApiResp;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/me/pets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OwnerPetController {

  private final OwnerPetService ownerPetService;

  @GetMapping
  public ApiResp<Page<PetDto>> getMyPets(
      Authentication authentication,
      Pageable pageable) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    return ApiResp.<Page<PetDto>>builder()
        .message("Lấy danh sách thú cưng thành công")
        .data(ownerPetService.getMyPets(
            currentUserId,
            pageable))
        .build();
  }

  @GetMapping("/{petId}")
  public ApiResp<PetDto> getMyPet(
      @PathVariable UUID petId,
      Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    return ApiResp.<PetDto>builder()
        .message("Lấy thông tin thú cưng thành công")
        .data(ownerPetService.getMyPet(
            petId,
            currentUserId))
        .build();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResp<PetDto> createMyPet(
      @Valid @RequestBody OwnerPetRequest request,
      Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    return ApiResp.<PetDto>builder()
        .message("Tạo hồ sơ thú cưng thành công")
        .data(ownerPetService.createMyPet(
            request,
            currentUserId))
        .build();
  }

  @PutMapping("/{petId}")
  public ApiResp<PetDto> updateMyPet(
      @PathVariable UUID petId,
      @Valid @RequestBody OwnerPetRequest request,
      Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    return ApiResp.<PetDto>builder()
        .message("Cập nhật thú cưng thành công")
        .data(ownerPetService.updateMyPet(
            petId,
            request,
            currentUserId))
        .build();
  }

  @DeleteMapping("/{petId}")
  public ApiResp<Void> deleteMyPet(
      @PathVariable UUID petId,
      Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    ownerPetService.deleteMyPet(
        petId,
        currentUserId);

    return ApiResp.<Void>builder()
        .message("Xóa thú cưng thành công")
        .data(null)
        .build();
  }
}
