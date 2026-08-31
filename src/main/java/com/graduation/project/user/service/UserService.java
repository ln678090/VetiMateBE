package com.graduation.project.user.service;

import com.graduation.project.user.dto.req.UpdateProfileRequest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import com.graduation.project.user.dto.resp.UserProfileResp;

public interface UserService {
    @Transactional
    void updateProfile(UUID userId, UpdateProfileRequest request);
    UserProfileResp getMyProfile(UUID userId);
}
