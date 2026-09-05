package com.graduation.project.user.service.impl;

import com.graduation.project.user.dto.req.UpdateProfileRequest;
import com.graduation.project.user.dto.resp.UserProfileResp;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import com.graduation.project.user.service.UserService;
import com.graduation.project.staff.service.StaffService;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.entity.Role;
import com.graduation.project.staff.dto.req.CreateStaffRequest;
import com.graduation.project.staff.entity.StaffRoleType;
import com.graduation.project.user.dto.req.AdminCreateUserReq;
import com.graduation.project.user.dto.resp.UserAdminResp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import java.util.UUID;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final StaffService staffService;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  @Override
  public void updateProfile(UUID userId, UpdateProfileRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

    if (user.getPhone() != null
        && request.phone() != null
        && !user.getPhone().equals(request.phone())
        && userRepository.existsByPhone(request.phone())) {
      throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác");
    }

    if (request.username() != null
        && !request.username().equals(user.getUsername())
        && userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username đã được sử dụng bởi tài khoản khác");
    }

    user.setFullName(request.fullName());
    user.setUsername(request.username());
    user.setPhone(request.phone());

    userRepository.save(user);
  }

  @Override
  public UserProfileResp getMyProfile(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

    return UserProfileResp.builder()
        .id(user.getId())
        .fullName(user.getFullName())
        .username(user.getUsername())
        .email(user.getEmail())
        .phone(user.getPhone())
        .build();
  }

  @Override
  public org.springframework.data.domain.Page<com.graduation.project.user.dto.resp.UserAdminResp> getAllUsers(org.springframework.data.domain.Pageable pageable) {
    return userRepository.findNonAdminUsers(pageable).map(user -> 
      com.graduation.project.user.dto.resp.UserAdminResp.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .enabled(user.getIsEnabled())
        .createdAt(user.getCreatedAt())
        .roles(user.getRoles().stream().map(r -> r.getName()).toList())
        .build()
    );
  }

  @Transactional
  @Override
  public void adminChangePassword(UUID targetUserId, String newPassword) {
    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    
    org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = 
        org.springframework.security.crypto.argon2.Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }



  @Transactional
  @Override
  public void toggleUserStatus(UUID targetUserId) {
    User user = userRepository.findById(targetUserId)
        .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    
    user.setEnabled(!user.getIsEnabled());
    userRepository.save(user);
  }

  @Transactional
  @Override
  public UserAdminResp adminCreateUser(AdminCreateUserReq req, UUID actorUserId) {
    if (userRepository.existsByUsername(req.username())) {
      throw new IllegalArgumentException("Username đã tồn tại");
    }
    if (req.email() != null && !req.email().isBlank() && userRepository.existsByEmail(req.email())) {
      throw new IllegalArgumentException("Email đã tồn tại");
    }
    if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
      throw new IllegalArgumentException("Số điện thoại đã tồn tại");
    }

    User user = new User();
    user.setUsername(req.username());
    user.setFullName(req.fullName());
    user.setEmail(req.email());
    user.setPhone(req.phone());
    user.setEnabled(true);
    
    PasswordEncoder passwordEncoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    user.setPassword(passwordEncoder.encode(req.password()));

    user = userRepository.save(user);

    List<String> staffRoleNames = List.of(
        "ROLE_DOCTOR", "ROLE_RECEPTIONIST", "ROLE_MANAGER", 
        "ROLE_ACCOUNTANT", "ROLE_WAREHOUSE", "ROLE_SHOP_STAFF"
    );

    if (staffRoleNames.contains(req.roleName())) {
      // Create staff -> implicitly assigns role
      StaffRoleType staffRoleType = switch (req.roleName()) {
        case "ROLE_DOCTOR" -> StaffRoleType.DOCTOR;
        case "ROLE_RECEPTIONIST" -> StaffRoleType.RECEPTIONIST;
        case "ROLE_MANAGER" -> StaffRoleType.MANAGER;
        case "ROLE_ACCOUNTANT" -> StaffRoleType.ACCOUNTANT;
        case "ROLE_WAREHOUSE" -> StaffRoleType.WAREHOUSE;
        case "ROLE_SHOP_STAFF" -> StaffRoleType.SHOP_STAFF;
        default -> throw new IllegalArgumentException("Invalid staff role");
      };
      CreateStaffRequest staffReq = new CreateStaffRequest(
          user.getId(), 
          staffRoleType, 
          "Tạo tài khoản từ Admin"
      );
      staffService.create(staffReq, actorUserId);
    } else {
      // Regular role (ADMIN, USER, SYSTEM_PARTNER)
      Role role = roleRepository.findByName(req.roleName())
          .orElseThrow(() -> new IllegalArgumentException("Quyền không tồn tại: " + req.roleName()));
      user.getRoles().add(role);
      userRepository.save(user);
    }

    // Refresh user from DB to get roles accurately
    user = userRepository.findById(user.getId()).orElseThrow();

    return UserAdminResp.builder()
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .fullName(user.getFullName())
        .phone(user.getPhone())
        .enabled(user.getIsEnabled())
        .createdAt(user.getCreatedAt())
        .roles(user.getRoles().stream().map(Role::getName).toList())
        .build();
  }
}
