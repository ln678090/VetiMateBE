package com.graduation.project.auth.keys;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "refresh-token")
public record RefreshTokenPrefix(String key) {}
