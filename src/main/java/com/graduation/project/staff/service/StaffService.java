
package com.graduation.project.staff.service;

import com.graduation.project.staff.dto.EligibleUserResponse;
import com.graduation.project.staff.dto.StaffResponse;
import com.graduation.project.staff.dto.req.CreateStaffRequest;
import com.graduation.project.staff.dto.req.UpdateStaffRequest;
import com.graduation.project.staff.entity.StaffRoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StaffService {

  StaffResponse create(
      CreateStaffRequest request,
      UUID actorUserId);

  StaffResponse update(
      UUID staffId,
      UpdateStaffRequest request,
      UUID actorUserId);

  StaffResponse getById(UUID staffId);

  Page<StaffResponse> search(
      String keyword,
      StaffRoleType roleType,
      Boolean active,
      Pageable pageable);

  StaffResponse deactivate(
      UUID staffId,
      String reason,
      UUID actorUserId);

  Page<EligibleUserResponse> searchEligibleUsers(
      String keyword,
      Pageable pageable);
}
