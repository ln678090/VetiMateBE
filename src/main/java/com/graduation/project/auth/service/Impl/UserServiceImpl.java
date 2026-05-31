package com.graduation.project.auth.service.Impl;

import com.graduation.project.auth.repository.RoleRepository;
import com.graduation.project.auth.service.UserService;
import com.graduation.project.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  final UserRepository userRepository;
  // private final UserMapper userMapper;
  // private final FriendshipRepository friendshipRepository;
  // private final FriendRequestRepository friendRequestRepository;
  // private final FollowRepository followRepository;
  // private final ChatRealtimeService chatRealtimeService;
  private final EntityManager entityManager;
  private final RoleRepository roleRepository;
  //     @Transactional
  //     @Override
  //     public void updateAvatar(UUID id, String avatarUrl) {
  //         User user = userRepository.findById(id).orElseThrow(() -> new
  // IllegalArgumentException("Không tìm thấy user"));
  //         user.setAvatarUrl(avatarUrl);
  // //        userRepository.save(user);
  //         chatRealtimeService.syncUpdateProfileToChatService(id, null, avatarUrl, null);
  //     }

  //     @Transactional
  //     @Override
  //     public void updateCover(UUID id, String coverUrl) {
  //         User user = userRepository.findById(id).orElseThrow(() -> new
  // IllegalArgumentException("Không tìm thấy user"));
  //         user.setCoverUrl(coverUrl);
  //         userRepository.save(user);
  //     }

  // @Override
  // public UserProfileResp getProfile(String id) {
  //     User user = userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new
  // IllegalArgumentException("User not found"));
  //     return userMapper.toUserProfileResp(user);
  // }

  // @Transactional
  // @Override
  // public UserProfileResp getProfile(UUID currentUserId, String targetIdStr) {
  //     UUID targetUserId = UUID.fromString(targetIdStr);
  //     User user = User.builder().
  //             id(targetUserId).fullName("foo").
  //             build();
  //     entityManager.persist(user);

  //     User targetUser = userRepository.findById(targetUserId)
  //             .orElseThrow(() -> new IllegalArgumentException("User not found"));

  //     String status = "NONE";

  //     if (currentUserId != null) {
  //         if (currentUserId.equals(targetUserId)) {
  //             status = "SELF";
  //         } else if (friendshipRepository.existsById(new FriendshipId(currentUserId,
  // targetUserId))) {
  //             status = "FRIENDS";
  //         } else if
  // (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(currentUserId, targetUserId,
  // FriendRequestStatus.PENDING)) {
  //             status = "REQUEST_SENT"; // Mình đã gửi
  //         } else if (friendRequestRepository.existsBySenderIdAndReceiverIdAndStatus(targetUserId,
  // currentUserId, FriendRequestStatus.PENDING)) {
  //             status = "REQUEST_RECEIVED"; // Người ta gửi cho mình
  //         }
  //     }
  //     long followerCount = followRepository.countByFollowingId(targetUserId);
  //     long followingCount = followRepository.countByFollowerId(targetUserId);
  //     boolean isFollowing = currentUserId != null &&
  // followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
  //     boolean isOnline = false;

  //     return new UserProfileResp(
  //             targetUser.getFullName(), targetUser.getBio(), targetUser.getLocation(),
  //             targetUser.getWebsiteUrl(), targetUser.getAvatarUrl(), targetUser.getCoverUrl(),
  // status,
  //             followerCount, followingCount, isFollowing, isOnline
  //     );
  // }
  // @Override
  // @Transactional
  // public Role role() {
  //     var role =  roleRepository.findById(1).orElseThrow(() -> new IllegalArgumentException("User
  // not found"));
  //     return role;
  // }

  // @Override
  // @Transactional
  // public void updateProfileDetails(UUID userId, String fullName, String bio, String location,
  // String websiteUrl) {
  //     User user = userRepository.findById(userId)
  //             .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

  //     user.setFullName(fullName.trim());
  //     user.setBio(bio != null ? bio.trim() : null);
  //     user.setLocation(location != null ? location.trim() : null);
  //     user.setWebsiteUrl(websiteUrl != null ? websiteUrl.trim() : null);

  //     userRepository.save(user);
  //     chatRealtimeService.syncUpdateProfileToChatService(
  //             userId,
  //             fullName,
  //             user.getAvatarUrl(),
  //             location
  //     );
  // }

  // @Override
  // public List<UserFindUserResp> searchUsers(String query, UUID currentUserId) {
  //     // Gọi query trong DB
  //     List<User> users = userRepository.searchUsers(query, currentUserId);

  //     // Chuyển đổi Entity sang DTO
  //     return users.stream()
  //             .map(u -> new UserFindUserResp(
  //                     u.getId(),
  //                     u.getFullName(),
  //                     u.getUsername(),
  //                     u.getAvatarUrl(), // Cẩn thận tên trường (bên ConnectHub thường là
  // avatarUrl)
  //                     Boolean.TRUE.equals(true)
  //             ))
  //             .collect(Collectors.toList());
  // }
}
