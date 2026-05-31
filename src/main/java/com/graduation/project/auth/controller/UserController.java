// package org.ln678090.connecthub.auth.controller;

// import java.util.Map;
// import java.util.UUID;

// import org.springframework.http.ResponseEntity;
// import org.springframework.security.core.Authentication;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.graduation.project.auth.dto.req.UpdateProfileRequest;
// import com.graduation.project.auth.dto.resp.UserProfileResp;
// import com.graduation.project.auth.service.UserService;
// import com.graduation.project.auth.utils.SecurityUtils;
// import com.graduation.project.common.resp.ApiResp;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequiredArgsConstructor
// @RequestMapping("/api/users")
// public class UserController {

//   private final UserService userService;

// //   @GetMapping("/1")
// //   public Role role(){
// //       return userService.role();
// //   }
// //    @GetMapping("/profile/{id}")
// //    public ResponseEntity<?> getProfile(
// //            @PathVariable @RequestPart String id
// //    ) {
// //        UserProfileResp resp=userService.getProfile(id);
// //
// //   return ResponseEntity.ok(ApiResp.builder().message("success").data(resp).build());
// //    }
//     // UserControllerToChat.java
//     @GetMapping("/profile/{id}")
//     public ResponseEntity<?> getProfile(@PathVariable String id, Authentication authentication) {

//         UUID currentUserId = authentication != null ? SecurityUtils.currentUserId(authentication)
// : null;

//         UserProfileResp resp = userService.getProfile(currentUserId, id);
//         return ResponseEntity.ok(ApiResp.builder().message("success").data(resp).build());
//     }
//     @PutMapping("/profile/avatar")
//     public ResponseEntity<?> updateAvatar(@RequestBody Map<String, String> body,
//                                           Authentication authentication) {
//         String avatarUrl = body.get("avatarUrl");
//         if(avatarUrl==null) throw new RuntimeException("AvatarUrl is null");
//         userService.updateAvatar(SecurityUtils.currentUserId(authentication), avatarUrl);
//         return ResponseEntity.ok(ApiResp.<String>builder()
//                 .message("Cập nhật avatar thành công")
//                 .data(avatarUrl)
//                 .build());
//     }

//     @PutMapping("/profile/cover")
//     public ResponseEntity<?> updateCover(@RequestBody Map<String, String> body,
//                                          Authentication authentication) {
//         String coverUrl = body.get("coverUrl");
//         if(coverUrl==null) throw new RuntimeException("CoverUrl is null");
//         userService.updateCover(SecurityUtils.currentUserId(authentication), coverUrl);
//         return ResponseEntity.ok(ApiResp.<String>builder()
//                 .message("Cập nhật ảnh bìa thành công")
//                 .data(coverUrl)
//                 .build());
//     }
//     @PutMapping("/profile")
//     public ResponseEntity<ApiResp<String>> updateProfile(
//             @Valid @RequestBody UpdateProfileRequest request,
//             Authentication authentication) {

//         UUID currentUserId = SecurityUtils.currentUserId(authentication);

//         userService.updateProfileDetails(
//                 currentUserId,
//                 request.fullName(),
//                 request.bio(),
//                 request.location(),
//                 request.websiteUrl()
//         );

//         return ResponseEntity.ok(ApiResp.<String>builder()
//                 .message("Cập nhật thông tin cá nhân thành công")
//                 .data("Success")
//                 .build());
//     }
// }
