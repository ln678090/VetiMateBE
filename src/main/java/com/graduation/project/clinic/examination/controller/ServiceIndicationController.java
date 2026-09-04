package com.graduation.project.clinic.examination.controller;

import com.graduation.project.auth.utils.SecurityUtils;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.CompleteRequest;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.CreateRequest;
import com.graduation.project.clinic.examination.dto.ServiceIndicationDtos.Response;
import com.graduation.project.clinic.examination.service.ServiceIndicationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/examinations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class ServiceIndicationController {

  private final ServiceIndicationService indicationService;

  @GetMapping("/{medicalRecordId}/indications")
  public List<Response> getAll(@PathVariable UUID medicalRecordId, Authentication authentication) {
    return indicationService.getAll(medicalRecordId, SecurityUtils.currentUserId(authentication));
  }

  @PostMapping("/{medicalRecordId}/indications")
  public Response create(
      @PathVariable UUID medicalRecordId,
      @Valid @RequestBody CreateRequest request,
      Authentication authentication) {
    return indicationService.create(
        medicalRecordId, request, SecurityUtils.currentUserId(authentication));
  }

  @PutMapping("/indications/{indicationId}/complete")
  public Response complete(
      @PathVariable UUID indicationId,
      @Valid @RequestBody CompleteRequest request,
      Authentication authentication) {
    return indicationService.complete(
        indicationId, request, SecurityUtils.currentUserId(authentication));
  }

  @DeleteMapping("/indications/{indicationId}")
  public Response cancel(@PathVariable UUID indicationId, Authentication authentication) {
    return indicationService.cancel(indicationId, SecurityUtils.currentUserId(authentication));
  }
}
