package com.graduation.project.auth.dto.resp;

import java.util.UUID;

public record MyProfileResp(
    UUID id, String fullName, String username, String email, String phone) {}
