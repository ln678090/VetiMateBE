package com.graduation.project.staff.service.impl;

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
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffServiceImpl implements StaffService {

  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final RoleAssignmentService roleAssignmentService;

  @Override
  @Transactional
  public StaffResponse create(CreateStaffRequest request, UUID actorUserId) {
    validateUserLinkAvailable(request.userId());

    User user = findUser(request.userId());

    Staff staff = new Staff();
    staff.setUser(user);

    /*
     * User là nguồn dữ liệu identity duy nhất.
     * Staff chỉ lưu snapshot để tương thích schema V10.
     */
    staff.setFullName(user.getFullName());
    staff.setPhone(user.getPhone());

    staff.setRoleType(request.roleType());
    staff.setLicenseNumber(null);
    staff.setBaseSalary(BigDecimal.ZERO);
    staff.setCommissionRate(BigDecimal.ZERO);
    staff.setActive(true);

    /*
     * Flush trước khi gọi RoleAssignmentService để các kiểm tra
     * Staff/User trong service phân quyền nhìn thấy Staff vừa tạo.
     *
     * Nếu phân quyền hoặc audit thất bại, toàn bộ transaction
     * vẫn rollback, bao gồm bản ghi Staff vừa insert.
     */
    Staff savedStaff = staffRepository.saveAndFlush(staff);

    roleAssignmentService.assignStaffRole(
        user.getId(), savedStaff.getRoleType(), actorUserId, request.reason());

    return toResponse(savedStaff);
  }

  @Override
  @Transactional
  public StaffResponse update(UUID staffId, UpdateStaffRequest request, UUID actorUserId) {
    Staff staff = findStaff(staffId);
    User linkedUser = requireLinkedUser(staff);

    /*
     * Deactivate phải đi qua command riêng để chắc chắn workforce
     * role cũng được thu hồi trong cùng transaction.
     */
    if (!request.active()) {
      throw new StaffConflictException("Hãy dùng chức năng ngừng hoạt động nhân viên");
    }

    boolean roleChanged = staff.getRoleType() != request.roleType();

    boolean reactivating = !staff.isActive();

    if (!roleChanged && !reactivating) {
      return toResponse(staff);
    }

    /*
     * Cập nhật Staff trước để RoleAssignmentService có thể xác minh:
     *
     * active Staff.roleType phải khớp với workforce role được cấp.
     */
    staff.setRoleType(request.roleType());
    staff.setActive(true);

    Staff savedStaff = staffRepository.saveAndFlush(staff);

    /*
     * Service phân quyền phải:
     * - từ chối tài khoản ROLE_ADMIN;
     * - xóa ROLE_USER và workforce role cũ;
     * - cấp đúng workforce role mới;
     * - không nhận role tùy ý từ client;
     * - ghi audit;
     * - thu hồi refresh session.
     */
    roleAssignmentService.assignStaffRole(
        linkedUser.getId(), savedStaff.getRoleType(), actorUserId, request.reason());

    return toResponse(savedStaff);
  }

  @Override
  public StaffResponse getById(UUID staffId) {
    return toResponse(findStaff(staffId));
  }

  @Override
  public Page<StaffResponse> search(
      String keyword, StaffRoleType roleType, Boolean active, Pageable pageable) {
    String normalizedKeyword = normalizeKeyword(keyword);

    boolean hasNoFilters = normalizedKeyword.isEmpty() && roleType == null && active == null;

    Page<Staff> staffPage =
        hasNoFilters
            ? staffRepository.findAll(pageable)
            : staffRepository.search(normalizedKeyword, roleType, active, pageable);

    return staffPage.map(this::toResponse);
  }

  @Override
  @Transactional
  public StaffResponse deactivate(UUID staffId, String reason, UUID actorUserId) {
    Staff staff = findStaff(staffId);
    User linkedUser = requireLinkedUser(staff);

    if (!staff.isActive()) {
      return toResponse(staff);
    }

    /*
     * Đổi trạng thái Staff trước để không tồn tại active Staff
     * sau khi workforce role đã bị thu hồi.
     */
    staff.setActive(false);

    Staff savedStaff = staffRepository.saveAndFlush(staff);

    /*
     * Chỉ thu hồi workforce roles.
     * Không xóa User, không cấp ROLE_USER ngầm và không đụng ROLE_ADMIN.
     */
    roleAssignmentService.revokeStaffRoles(linkedUser.getId(), actorUserId, reason);

    return toResponse(savedStaff);
  }

  @Override
  public Page<EligibleUserResponse> searchEligibleUsers(String keyword, Pageable pageable) {
    String normalizedKeyword = normalizeKeyword(keyword);

    /*
     * Repository phải loại:
     * - User đã liên kết với Staff;
     * - User có ROLE_ADMIN;
     * - User bị disabled nếu chính sách onboarding yêu cầu.
     */
    return userRepository
        .findEligibleForStaff(normalizedKeyword, pageable)
        .map(this::toEligibleUserResponse);
  }

  private Staff findStaff(UUID staffId) {
    return staffRepository
        .findById(staffId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy nhân viên với ID: " + staffId));
  }

  private User findUser(UUID userId) {
    if (userId == null) {
      throw new StaffConflictException("Tài khoản liên kết nhân viên là bắt buộc");
    }

    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Không tìm thấy tài khoản với ID: " + userId));
  }

  private User requireLinkedUser(Staff staff) {
    User user = staff.getUser();

    if (user == null) {
      throw new StaffConflictException("Nhân viên chưa liên kết với tài khoản");
    }

    return user;
  }

  private void validateUserLinkAvailable(UUID userId) {
    if (userId == null) {
      throw new StaffConflictException("Tài khoản liên kết nhân viên là bắt buộc");
    }

    if (staffRepository.existsByUserId(userId)) {
      throw new StaffConflictException("Tài khoản đã được liên kết với nhân viên khác");
    }
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) {
      return "";
    }

    return keyword.trim().toLowerCase(Locale.ROOT);
  }

  private StaffResponse toResponse(Staff staff) {
    UUID userId = staff.getUser() == null ? null : staff.getUser().getId();

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

  private EligibleUserResponse toEligibleUserResponse(User user) {
    return new EligibleUserResponse(
        user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getPhone());
  }
}
