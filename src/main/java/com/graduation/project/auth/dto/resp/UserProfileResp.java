package com.graduation.project.auth.dto.resp;

public record UserProfileResp(
    String fullName,
    String bio,
    String location,
    String websiteUrl,
    String avatarUrl,
    String coverUrl,
    String friendshipStatus, // "NONE", "FRIENDS", "REQUEST_SENT", "REQUEST_RECEIVED", "SELF"
    long followerCount,
    long followingCount,
    boolean isFollowing, //  mình có đang theo dõi họ không
    boolean isOnline) {}
