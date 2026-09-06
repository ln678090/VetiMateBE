
package com.graduation.project.staff.service.impl;

import com.graduation.project.audit.dto.AuditLogEvent;
import com.graduation.project.audit.entity.AuditAction;
import com.graduation.project.audit.service.AuditLogWriter;
import com.graduation.project.auth.service.RoleAssignmentService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.staff.dto.EligibleUserResponse;
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

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService {

  private static final String AUDIT_MODULE = "STAFF";
  private static final String AUDIT_TABLE = "staff";

  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final RoleAssignmentService roleAssignmentService;
  private final AuditLogWriter auditLogWriter;

  @Override
  @Transactional
  public StaffResponse create(
      CreateStaffRequest request,
      UUID actorUserId) {
    validateUserLinkAvailable(request.userId());

    User user = findUser(request.userId());

    Staff staff = new Staff();
    staff.setUser(user);

    /*
     * User là nguồn dữ liệu identity duy nhất.
     * Staff chỉ giữ snapshot tương thích schema V10.
     */
    staff.setFullName(user.getFullName());
    staff.setPhone(user.getPhone());
    staff.setRoleType(request.roleType());
    staff.setLicenseNumber(null);
    staff.setBaseSalary(BigDecimal.ZERO);
    staff.setCommissionRate(BigDecimal.ZERO);
    staff.setActive(true);

    /*
     * Flush để RoleAssignmentService nhìn thấy Staff vừa tạo.
     * Nếu phân quyền hoặc audit thất bại, transaction rollback.
     */
    Staff savedStaff = staffRepository.saveAndFlush(staff);

    roleAssignmentService.assignStaffRole(
        user.getId(),
        savedStaff.getRoleType(),
        actorUserId,
        request.reason());

    auditLogWriter.record(
        new AuditLogEvent(
            AUDIT_MODULE,
            AUDIT_TABLE,
            savedStaff.getId(),
            AuditAction.INSERT,
            null,
            toAuditSnapshot(savedStaff),
            actorUserId,
            resolveActorIdentifier(actorUserId),
            "createEmployee",
            null,
            null));

    return toResponse(savedStaff);
  }

  @Override
  @Transactional
  public StaffResponse update(
      UUID staffId,
      UpdateStaffRequest request,
      UUID actorUserId) {
    Staff staff = findStaff(staffId);
    User linkedUser = requireLinkedUser(staff);

    /*
     * Deactivate phải dùng command riêng để workforce role
     * được thu hồi trong cùng transaction.
     */
    if (!request.active()) {
      throw new StaffConflictException(
          "Hãy dùng chức năng ngừng hoạt động nhân viên");
    }

    boolean roleChanged = staff.getRoleType() != request.roleType();

    boolean reactivating = !staff.isActive();

    if (!roleChanged && !reactivating) {
      return toResponse(staff);
    }

    StaffAuditSnapshot oldData = toAuditSnapshot(staff);

    staff.setRoleType(request.roleType());
    staff.setActive(true);

    Staff savedStaff = staffRepository.saveAndFlush(staff);

    /*
     * RoleAssignmentService chịu trách nhiệm:
     * - từ chối ROLE_ADMIN;
     * - thu hồi workforce role cũ;
     * - cấp workforce role mới;
     * - thu hồi refresh session;
     * - ghi ROLE_CHANGE nếu đã triển khai.
     */
    roleAssignmentService.assignStaffRole(
        linkedUser.getId(),
        savedStaff.getRoleType(),
        actorUserId,
        request.reason());

    auditLogWriter.record(
        new AuditLogEvent(
            AUDIT_MODULE,
            AUDIT_TABLE,
            savedStaff.getId(),
            AuditAction.UPDATE,
            oldData,
            toAuditSnapshot(savedStaff),
            actorUserId,
            resolveActorIdentifier(actorUserId),
            roleChanged
                ? "Update employee role"
                : "Reactivate employee",
            null,
            null));

    return toResponse(savedStaff);
  }

  @Override
  public StaffResponse getById(UUID staffId) {
    return toResponse(findStaff(staffId));
  }

  @Override
  public Page<StaffResponse> search(
      String keyword,
      StaffRoleType roleType,
      Boolean active,
      Pageable pageable) {
    String normalizedKeyword = normalizeKeyword(keyword);

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
  public StaffResponse deactivate(
      UUID staffId,
      String reason,
      UUID actorUserId) {
    Staff staff = findStaff(staffId);
    User linkedUser = requireLinkedUser(staff);

    if (!staff.isActive()) {
      return toResponse(staff);
    }

    StaffAuditSnapshot oldData = toAuditSnapshot(staff);

    staff.setActive(false);

    Staff savedStaff = staffRepository.saveAndFlush(staff);

    /*
     * Chỉ thu hồi workforce roles.
     * Không xóa User và không đụng ROLE_ADMIN.
     */
    roleAssignmentService.revokeStaffRoles(
        linkedUser.getId(),
        actorUserId,
        reason);

    auditLogWriter.record(
        new AuditLogEvent(
            AUDIT_MODULE,
            AUDIT_TABLE,
            savedStaff.getId(),
            AuditAction.DELETE,
            oldData,
            toAuditSnapshot(savedStaff),
            actorUserId,
            resolveActorIdentifier(actorUserId),
            "Deactivate employee: "
                + normalizeReason(reason),
            null,
            null));

    return toResponse(savedStaff);
  }

  @Override
  public Page<EligibleUserResponse> searchEligibleUsers(
      String keyword,
      Pageable pageable) {
    String normalizedKeyword = normalizeKeyword(keyword);

    /*
     * Repository phải loại:
     * - User đã liên kết với Staff;
     * - User có ROLE_ADMIN;
     * - User bị vô hiệu hóa nếu chính sách yêu cầu.
     */
    return userRepository
        .findEligibleForStaff(normalizedKeyword, pageable)
        .map(this::toEligibleUserResponse);
  }

  private Staff findStaff(UUID staffId) {
    return staffRepository
        .findById(staffId)
        .orElseThrow(
            () -> new ResourceNotFoundException(
                "Không tìm thấy nhân viên với ID: "
                    + staffId));
  }

  private User findUser(UUID userId) {
    if (userId == null) {
      throw new StaffConflictException(
          "Tài khoản liên kết nhân viên là bắt buộc");
    }

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException(
                "Không tìm thấy tài khoản với ID: "
                    + userId));
  }

  private User requireLinkedUser(Staff staff) {
    User user = staff.getUser();

    if (user == null) {
      throw new StaffConflictException(
          "Nhân viên chưa liên kết với tài khoản");
    }

    return user;
  }

  private void validateUserLinkAvailable(UUID userId) {
    if (userId == null) {
      throw new StaffConflictException(
          "Tài khoản liên kết nhân viên là bắt buộc");
    }

    if (staffRepository.existsByUserId(userId)) {
      throw new StaffConflictException(
          "Tài khoản đã được liên kết với nhân viên khác");
    }
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) {
      return "";
    }

    return keyword.trim().toLowerCase(Locale.ROOT);
  }

  private String normalizeReason(String reason) {
    if (reason == null || reason.isBlank()) {
      return "Không cung cấp lý do";
    }

    return reason.trim();
  }

  private String resolveActorIdentifier(UUID actorUserId) {
    if (actorUserId == null) {
      return "SYSTEM";
    }

    return userRepository
        .findById(actorUserId)
        .map(this::getUserIdentifier)
        .orElse(actorUserId.toString());
  }

  private String getUserIdentifier(User user) {
    if (user.getEmail() != null
        && !user.getEmail().isBlank()) {
      return user.getEmail();
    }

    if (user.getUsername() != null
        && !user.getUsername().isBlank()) {
      return user.getUsername();
    }

    return user.getId().toString();
  }

  private StaffResponse toResponse(Staff staff) {
    UUID userId = staff.getUser() == null
        ? null
        : staff.getUser().getId();

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

  private EligibleUserResponse toEligibleUserResponse(
      User user) {
    return new EligibleUserResponse(
        user.getId(),
        user.getUsername(),
        user.getFullName(),
        user.getEmail(),
        user.getPhone());
  }

  private StaffAuditSnapshot toAuditSnapshot(Staff staff) {
    UUID userId = staff.getUser() == null
        ? null
        : staff.getUser().getId();

    return new StaffAuditSnapshot(
        staff.getId(),
        userId,
        staff.getFullName(),
        staff.getPhone(),
        staff.getRoleType(),
        staff.getLicenseNumber(),
        staff.getBaseSalary(),
        staff.getCommissionRate(),
        staff.isActive());
  }

  private record StaffAuditSnapshot(
      UUID id,
      UUID userId,
      String fullName,
      String phone,
      StaffRoleType roleType,
      String licenseNumber,
      BigDecimal baseSalary,
      BigDecimal commissionRate,
      boolean active) {
  }
}
