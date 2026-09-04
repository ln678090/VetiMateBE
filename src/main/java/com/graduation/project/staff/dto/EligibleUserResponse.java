package com.graduation.project.staff.dto;

import java.util.UUID;

public record EligibleUserResponse(
    UUID id, String username, String fullName, String email, String phone) {}
