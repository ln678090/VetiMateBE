package com.graduation.project.auth.config.custom;

import com.graduation.project.user.entity.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(
    UUID id,
    String email,
    String password,
    String fullName,
    // String avatarUrl,
    // String coverUrl,
    // String bio,
    // String location,
    // String websiteUrl,
    boolean isEnabled,
    Collection<? extends GrantedAuthority> authorities)
    implements UserDetails {

  // Factory method gọn gàng để chuyển từ Entity -> Record
  public static CustomUserDetails fromUser(User user) {
    return new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getFullName(),
        user.getEnabled() != null ? user.getIsEnabled().booleanValue() : false,
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toSet()));
  }

  public String getRolesAsString() {
    if (authorities == null || authorities.isEmpty()) return "";

    return authorities.stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.joining(" "));
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return new ArrayList<>(authorities);
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return UserDetails.super.isAccountNonExpired();
  }

  @Override
  public boolean isAccountNonLocked() {
    return UserDetails.super.isAccountNonLocked();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return UserDetails.super.isCredentialsNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return isEnabled;
  }
}
