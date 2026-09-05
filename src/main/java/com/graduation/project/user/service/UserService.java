package com.graduation.project.user.service;

import com.graduation.project.user.dto.req.AdminChangePasswordReq;
import com.graduation.project.user.dto.req.AdminCreateUserReq;
import com.graduation.project.user.dto.req.UpdateProfileRequest;
import com.graduation.project.user.dto.resp.UserAdminResp;
import com.graduation.project.user.dto.resp.UserProfileResp;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
  @Transactional
  void updateProfile(UUID userId, UpdateProfileRequest request);

  UserProfileResp getMyProfile(UUID userId);

  org.springframework.data.domain.Page<UserAdminResp> getAllUsers(org.springframework.data.domain.Pageable pageable);

  @Transactional
  void adminChangePassword(UUID targetUserId, String newPassword);

  @Transactional
  void toggleUserStatus(UUID targetUserId);

  @Transactional
  UserAdminResp adminCreateUser(AdminCreateUserReq req, UUID actorUserId);
}
