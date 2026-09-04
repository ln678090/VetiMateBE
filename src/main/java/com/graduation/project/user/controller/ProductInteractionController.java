package com.graduation.project.user.controller;

import com.graduation.project.auth.config.custom.CustomUserDetails;
import com.graduation.project.common.resp.ApiResp;
import com.graduation.project.product.dto.resp.ProductResp;
import com.graduation.project.user.service.ProductInteractionService;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProductInteractionController {

  private final ProductInteractionService interactionService;

  private UUID getUserId(Authentication authentication) {
    Object principal = authentication.getPrincipal();
    if (principal instanceof Jwt jwt) {
      return UUID.fromString(jwt.getSubject());
    } else if (principal instanceof CustomUserDetails userDetails) {
      return userDetails.id();
    }
    throw new IllegalStateException("Authentication principal không hợp lệ");
  }

  @GetMapping("/favorites/check/{productId}")
  public ResponseEntity<ApiResp<Boolean>> checkFavorite(
      @PathVariable UUID productId, Authentication authentication) {
    return ResponseEntity.ok(
        ApiResp.<Boolean>builder()
            .message("Thành công")
            .data(interactionService.isFavorite(getUserId(authentication), productId))
            .timestamp(Instant.now().toString())
            .build());
  }

  @PostMapping("/favorites/{productId}")
  public ResponseEntity<ApiResp<String>> toggleFavorite(
      @PathVariable UUID productId, Authentication authentication) {
    interactionService.toggleFavorite(getUserId(authentication), productId);
    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build());
  }

  @PostMapping("/viewed/{productId}")
  public ResponseEntity<ApiResp<String>> recordView(
      @PathVariable UUID productId, Authentication authentication) {
    interactionService.recordView(getUserId(authentication), productId);
    return ResponseEntity.ok(
        ApiResp.<String>builder()
            .message("Thành công")
            .data("OK")
            .timestamp(Instant.now().toString())
            .build());
  }

  @GetMapping("/favorites")
  public ResponseEntity<ApiResp<Page<ProductResp>>> getFavorites(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) OffsetDateTime startDate,
      @RequestParam(required = false) OffsetDateTime endDate,
      Authentication authentication) {
    return ResponseEntity.ok(
        ApiResp.<Page<ProductResp>>builder()
            .message("Thành công")
            .data(
                interactionService.getFavoriteProducts(
                    getUserId(authentication), startDate, endDate, PageRequest.of(page, size)))
            .timestamp(Instant.now().toString())
            .build());
  }

  @GetMapping("/viewed")
  public ResponseEntity<ApiResp<Page<ProductResp>>> getRecentlyViewed(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(required = false) OffsetDateTime startDate,
      @RequestParam(required = false) OffsetDateTime endDate,
      Authentication authentication) {
    return ResponseEntity.ok(
        ApiResp.<Page<ProductResp>>builder()
            .message("Thành công")
            .data(
                interactionService.getRecentlyViewedProducts(
                    getUserId(authentication), startDate, endDate, PageRequest.of(page, size)))
            .timestamp(Instant.now().toString())
            .build());
  }
}
