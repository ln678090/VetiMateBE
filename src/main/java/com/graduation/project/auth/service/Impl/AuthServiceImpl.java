package com.graduation.project.auth.service.impl;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.auth.config.jwt.TokenService;
import com.graduation.project.auth.dto.privateDto.TokenPair;
import com.graduation.project.auth.dto.req.LoginRequest;
import com.graduation.project.auth.dto.req.RegisterRequest;
import com.graduation.project.auth.entity.Role;
import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.service.AuthService;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  // @Value("${app.google.client-id}")
  // private String googleClientId;

  // ... (Giữ nguyên hàm login, register hiện tại)

  // @Transactional
  // @Override
  // public TokenPair loginWithGoogle(String idTokenString) {
  // try {
  // GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new
  // NetHttpTransport(), new GsonFactory())
  // .setAudience(Collections.singletonList(googleClientId))
  // .build();

  // GoogleIdToken idToken = verifier.verify(idTokenString);
  // if (idToken == null) {
  // throw new RuntimeException("Token Google không hợp lệ hoặc đã hết hạn.");
  // }

  // GoogleIdToken.Payload payload = idToken.getPayload();
  // String email = payload.getEmail();
  // String name = (String) payload.get("name");
  // String pictureUrl = (String) payload.get("picture");

  // Optional<User> userOptional = userRepository.findByEmail(email); // Dùng hàm
  // findByEmail có sẵn
  // User user;

  // if (userOptional.isPresent()) {
  // user = userOptional.get();

  // // KIỂM TRA TÀI KHOẢN ĐÃ BỊ KHÓA HAY CHƯA
  // if (user.getIsEnabled() != null && !user.getIsEnabled()) {
  // throw new DisabledException("Tài khoản của bạn đã bị vô hiệu hóa.");
  // }
  // } else {
  // Role userRole = roleRepository.findByName("ROLE_USER")
  // .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại"));

  // // Sinh ra username tự động nếu entity yêu cầu
  // String prefix = email.contains("@") ? email.split("@")[0] :
  // UUID.randomUUID().toString().substring(0, 8);

  // // Mật khẩu ngẫu nhiên cho user đăng nhập Google
  // String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

  // user = User.builder()
  // .id(UuidCreator.getTimeOrderedEpoch())
  // .email(email)
  // .fullName(name)
  // .passwordHash(randomPassword)
  // .avatarUrl(pictureUrl)
  // .username(prefix) // Thêm username tự động
  // .isEnabled(Boolean.TRUE)
  // .createdAt(OffsetDateTime.now())
  // .updatedAt(OffsetDateTime.now())
  // .roles(List.of(userRole))
  // .build();
  // user = userRepository.save(user);
  // }

  // // Map qua CustomUserDetails để sinh Token
  // CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

  // // Trả về Access Token và Refresh Token
  // String accessToken = tokenService.generateAccessToken(userDetails.id(),
  // userDetails.getRolesAsString());
  // String refreshToken = tokenService.generateRefreshToken(userDetails.id(),
  // userDetails.getRolesAsString());

  // return new TokenPair(accessToken, refreshToken);

  // } catch (DisabledException e) {
  // throw e;
  // } catch (Exception e) {
  // throw new RuntimeException("Xác thực Google thất bại: " + e.getMessage(), e);
  // }
  // }
  // @Override
  // public TokenPair login(LoginRequest request) {
  // Authentication authentication =
  // authenticationManager.authenticate(
  // new UsernamePasswordAuthenticationToken(request.email(),
  // request.password()));
  // CustomUserDetails userDetails = (CustomUserDetails)
  // authentication.getPrincipal();
  // String roles = userDetails.getRolesAsString();
  // String accessToken = tokenService.generateAccessToken(userDetails.id(),
  // roles);
  // String refreshToken = tokenService.generateRefreshToken(userDetails.id(),
  // roles);
  // return new TokenPair(accessToken, refreshToken);
  // }
  @Override
  public TokenPair login(LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.email(),
            request.password()));

    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

    String accessToken = tokenService.generateAccessToken(
        userDetails.id(),
        userDetails.getRolesAsString());

    String refreshToken = tokenService.generateRefreshToken(userDetails.id());

    return new TokenPair(accessToken, refreshToken);
  }

  @Transactional
  @Override
  public TokenPair register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email already exists ");
    }
    if (userRepository.existsByPhone(request.phone())) {
      throw new IllegalArgumentException("Phone already exists ");
    }
    Role userRole = roleRepository
        .findByName("ROLE_USER")
        .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại "));

    // Role userRole1 = roleRepository.findByName("ROLE_USER")
    // .orElseThrow(() -> new RuntimeException("Role mặc định không tồn tại "));

    User newUser = User.builder()
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .fullName(request.fullName())
        .username(request.username())
        .phone(request.phone())
        .enabled(Boolean.TRUE)
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .roles(List.of(userRole))
        .build();
    newUser = userRepository.save(newUser);

    CustomUserDetails userDetails = CustomUserDetails.fromUser(newUser);

    String accessToken = tokenService.generateAccessToken(
        userDetails.id(),
        userDetails.getRolesAsString());

    String refreshToken = tokenService.generateRefreshToken(userDetails.id());

    return new TokenPair(accessToken, refreshToken);
  }

  @Transactional(readOnly = true)
  @Override
  public TokenPair refreshToken(String oldRefreshToken) {
    UUID userId = tokenService.getUserIdFromRefreshToken(oldRefreshToken);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BadCredentialsException(
            "Người dùng của Refresh Token không còn tồn tại"));

    CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

    if (!userDetails.isEnabled()) {
      throw new BadCredentialsException(
          "Tài khoản đã bị vô hiệu hóa");
    }

    String newAccessToken = tokenService.generateAccessToken(
        userDetails.id(),
        userDetails.getRolesAsString());

    return new TokenPair(newAccessToken, oldRefreshToken);

  }

  @Override
  public void logout(String refreshToken) {
    if (refreshToken == null || refreshToken.isEmpty())
      return;
    tokenService.deleteRefreshToken(refreshToken);
  }
}
