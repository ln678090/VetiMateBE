package com.graduation.project.clinic.examination.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.examination.dto.ExaminationDtos.*;
import com.graduation.project.clinic.examination.service.ExaminationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinic/examinations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class ExaminationController {
  private final ExaminationService examinationService;

  @GetMapping("/history")
  @PreAuthorize("hasAuthority('ROLE_DOCTOR')")
  public Page<ExaminationHistoryResponse> getHistory(
      Authentication authentication,
      @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);

    return examinationService.getHistory(
        currentUserId,
        pageable);
  }

  @PostMapping("/appointments/{appointmentId}")
  public MedicalRecordResponse openExamination(
      @PathVariable UUID appointmentId,
      Authentication authentication) {
    UUID currentUserId = SecurityUtils.currentUserId(authentication);
    return examinationService.openExamination(
        appointmentId,
        currentUserId);
  }

  @GetMapping("/medicines")
  public List<MedicineOptionResponse> getMedicines() {
    return examinationService.getMedicines();
  }

  @GetMapping("/{medicalRecordId}")
  public MedicalRecordResponse getById(
      @PathVariable UUID medicalRecordId,
      Authentication authentication) {
    return examinationService.getById(
        medicalRecordId,
        SecurityUtils.currentUserId(authentication));
  }

  @PutMapping("/{medicalRecordId}")
  public MedicalRecordResponse saveExamination(
      @PathVariable UUID medicalRecordId,
      @Valid @RequestBody SaveExaminationRequest request,
      Authentication authentication) {
    return examinationService.saveExamination(
        medicalRecordId,
        request,
        SecurityUtils.currentUserId(authentication));
  }

  @PutMapping("/{medicalRecordId}/prescriptions")
  public MedicalRecordResponse replacePrescriptions(
      @PathVariable UUID medicalRecordId,
      @Valid @RequestBody ReplacePrescriptionsRequest request,
      Authentication authentication) {
    return examinationService.replacePrescriptions(
        medicalRecordId,
        request,
        SecurityUtils.currentUserId(authentication));
  }

  @PostMapping("/{medicalRecordId}/complete")
  public MedicalRecordResponse complete(
      @PathVariable UUID medicalRecordId,
      Authentication authentication) {
    return examinationService.complete(
        medicalRecordId,
        SecurityUtils.currentUserId(authentication));
  }
}
