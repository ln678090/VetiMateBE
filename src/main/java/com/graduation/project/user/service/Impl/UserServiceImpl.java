package com.graduation.project.user.service.Impl;

import com.graduation.project.user.dto.req.UpdateProfileRequest;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import com.graduation.project.user.service.UserService;
import com.graduation.project.user.dto.resp.UserProfileResp;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public void updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (user.getPhone() != null && request.phone() != null && !user.getPhone().equals(request.phone()) && userRepository.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác");
        }
        
        if (request.username() != null && !request.username().equals(user.getUsername()) && userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username đã được sử dụng bởi tài khoản khác");
        }

        user.setFullName(request.fullName());
        user.setUsername(request.username());
        user.setPhone(request.phone());

        userRepository.save(user);
    }

    @Override
    public UserProfileResp getMyProfile(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
            
        return UserProfileResp.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .username(user.getUsername())
            .email(user.getEmail())
            .phone(user.getPhone())
            .build();
    }
}
