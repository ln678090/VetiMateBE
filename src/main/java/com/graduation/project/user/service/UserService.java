package com.graduation.project.user.service;

import com.graduation.project.user.dto.req.UpdateProfileRequest;
import com.graduation.project.user.dto.resp.UserProfileResp;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
  @Transactional
  void updateProfile(UUID userId, UpdateProfileRequest request);

  UserProfileResp getMyProfile(UUID userId);
}
