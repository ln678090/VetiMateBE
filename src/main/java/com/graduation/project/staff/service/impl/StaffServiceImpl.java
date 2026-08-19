
package com.graduation.project.staff.service.impl;

import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.staff.dto.StaffResponse;
import com.graduation.project.staff.dto.req.CreateStaffRequest;
import com.graduation.project.staff.dto.req.UpdateStaffRequest;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.entity.StaffRoleType;
import com.graduation.project.staff.exception.StaffConflictException;
import com.graduation.project.staff.repository.StaffRepository;
import com.graduation.project.staff.service.StaffService;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService {

  private final StaffRepository staffRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public StaffResponse create(CreateStaffRequest request) {
    validateUserLinkAvailable(request.userId(), null);

    Staff staff = new Staff();
    staff.setUser(resolveUser(request.userId()));
    staff.setFullName(normalizeRequired(request.fullName()));
    staff.setPhone(normalizeNullable(request.phone()));
    staff.setRoleType(request.roleType());
    staff.setLicenseNumber(
        normalizeNullable(request.licenseNumber()));
    staff.setBaseSalary(request.baseSalary());
    staff.setCommissionRate(request.commissionRate());
    staff.setActive(true);

    Staff savedStaff = staffRepository.save(staff);

    return toResponse(savedStaff);
  }

  @Override
  @Transactional
  public StaffResponse update(
      UUID staffId,
      UpdateStaffRequest request) {
    Staff staff = findStaff(staffId);

    validateUserLinkAvailable(
        request.userId(),
        staffId);

    staff.setUser(resolveUser(request.userId()));
    staff.setFullName(normalizeRequired(request.fullName()));
    staff.setPhone(normalizeNullable(request.phone()));
    staff.setRoleType(request.roleType());
    staff.setLicenseNumber(
        normalizeNullable(request.licenseNumber()));
    staff.setBaseSalary(request.baseSalary());
    staff.setCommissionRate(request.commissionRate());
    staff.setActive(request.active());

    Staff savedStaff = staffRepository.save(staff);

    return toResponse(savedStaff);
  }

  @Override
  public StaffResponse getById(UUID staffId) {
    Staff staff = findStaff(staffId);

    return toResponse(staff);
  }

  @Override
  public Page<StaffResponse> search(
      String keyword,
      StaffRoleType roleType,
      Boolean active,
      Pageable pageable) {
    String normalizedKeyword = keyword == null
        ? ""
        : keyword.trim().toLowerCase(Locale.ROOT);

    boolean hasNoFilters = normalizedKeyword.isEmpty()
        && roleType == null
        && active == null;

    Page<Staff> staffPage = hasNoFilters
        ? staffRepository.findAll(pageable)
        : staffRepository.search(
            normalizedKeyword,
            roleType,
            active,
            pageable);

    return staffPage.map(this::toResponse);
  }

  @Override
  @Transactional
  public void deactivate(UUID staffId) {
    Staff staff = findStaff(staffId);

    if (!staff.isActive()) {
      return;
    }

    staff.setActive(false);
    staffRepository.save(staff);
  }

  private Staff findStaff(UUID staffId) {
    return staffRepository
        .findById(staffId)
        .orElseThrow(
            () -> new ResourceNotFoundException(
                "Không tìm thấy nhân viên với ID: "
                    + staffId));
  }

  private User resolveUser(UUID userId) {
    if (userId == null) {
      return null;
    }

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException(
                "Không tìm thấy tài khoản với ID: "
                    + userId));
  }

  private void validateUserLinkAvailable(
      UUID userId,
      UUID currentStaffId) {
    if (userId == null) {
      return;
    }

    boolean linkedToAnotherStaff;

    if (currentStaffId == null) {
      linkedToAnotherStaff = staffRepository.existsByUserId(userId);
    } else {
      linkedToAnotherStaff = staffRepository.existsByUserIdAndIdNot(
          userId,
          currentStaffId);
    }

    if (linkedToAnotherStaff) {
      throw new StaffConflictException(
          "Tài khoản đã được liên kết với nhân viên khác");
    }
  }

  private StaffResponse toResponse(Staff staff) {
    UUID userId = null;

    if (staff.getUser() != null) {
      userId = staff.getUser().getId();
    }

    return new StaffResponse(
        staff.getId(),
        userId,
        staff.getFullName(),
        staff.getPhone(),
        staff.getRoleType(),
        staff.getLicenseNumber(),
        staff.getBaseSalary(),
        staff.getCommissionRate(),
        staff.isActive(),
        staff.getCreatedAt());
  }

  private String normalizeRequired(String value) {
    return value.trim();
  }

  private String normalizeNullable(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();

    if (normalized.isEmpty()) {
      return null;
    }

    return normalized;
  }
}
