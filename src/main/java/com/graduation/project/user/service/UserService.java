package com.graduation.project.user.service;

import com.graduation.project.user.dto.UpdateProfileRequest;
import com.graduation.project.user.dto.UserProfileResp;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserProfileResp getMyProfile(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    return UserProfileResp.builder()
        .id(user.getId().toString())
        .fullName(user.getFullName())
        .username(user.getUsername())
        .email(user.getEmail())
        .phone(user.getPhone())
        .build();
  }

  @Transactional
  public void updateProfile(UUID userId, UpdateProfileRequest request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    user.setFullName(request.fullName());
    user.setUsername(request.username());
    user.setPhone(request.phone());

    userRepository.save(user);
  }
}
