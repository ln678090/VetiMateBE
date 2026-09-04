
package com.graduation.project.auth.service.Impl;

import com.graduation.project.auth.entity.Role;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.service.RoleAssignmentService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.staff.entity.StaffRoleType;
import com.graduation.project.staff.exception.StaffConflictException;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleAssignmentServiceImpl implements RoleAssignmentService {

  private static final String ROLE_ADMIN = "ROLE_ADMIN";
  private static final String ROLE_USER = "ROLE_USER";

  private static final Set<String> STAFF_ROLES =
      Set.of(
          "ROLE_DOCTOR",
          "ROLE_RECEPTIONIST",
          "ROLE_MANAGER",
          "ROLE_ACCOUNTANT",
          "ROLE_WAREHOUSE",
          "ROLE_SHOP_STAFF");

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public void revokeStaffRoles(UUID targetUserId, UUID actorUserId, String reason) {
    if (targetUserId.equals(actorUserId)) {
      throw new StaffConflictException("Không được tự thay đổi quyền của chính mình");
    }

    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Lý do thu hồi quyền không được để trống");
    }

    User user =
        userRepository
            .findById(targetUserId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + targetUserId));
    boolean adminAccount =
        user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equals(role.getName()));
    if (adminAccount) {
      throw new StaffConflictException("Không thể xử lý tài khoản Admin qua nghiệp vụ nhân sự");
    }

    user.getRoles().removeIf(role -> STAFF_ROLES.contains(role.getName()));
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void assignStaffRole(
      UUID targetUserId, StaffRoleType roleType, UUID actorUserId, String reason) {
    if (targetUserId.equals(actorUserId)) {
      throw new StaffConflictException("Không được tự thay đổi quyền của chính mình");
    }

    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Lý do thay đổi quyền không được để trống");
    }

    User user =
        userRepository
            .findById(targetUserId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy tài khoản: " + targetUserId));

    boolean adminAccount =
        user.getRoles().stream().anyMatch(role -> ROLE_ADMIN.equals(role.getName()));

    if (adminAccount) {
      throw new StaffConflictException("Không thể thay đổi tài khoản Admin qua nghiệp vụ nhân sự");
    }

    String targetRoleName = mapRoleName(roleType);

    Role targetRole =
        roleRepository
            .findByName(targetRoleName)
            .orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy quyền: " + targetRoleName));

    user.getRoles()
        .removeIf(role -> ROLE_USER.equals(role.getName()) || STAFF_ROLES.contains(role.getName()));

    user.getRoles().add(targetRole);

    userRepository.save(user);
  }

  private String mapRoleName(StaffRoleType roleType) {
    return switch (roleType) {
      case DOCTOR -> "ROLE_DOCTOR";
      case RECEPTIONIST -> "ROLE_RECEPTIONIST";
      case MANAGER -> "ROLE_MANAGER";
      case ACCOUNTANT -> "ROLE_ACCOUNTANT";
      case WAREHOUSE -> "ROLE_WAREHOUSE";
      case SHOP_STAFF -> "ROLE_SHOP_STAFF";
    };
  }
}
