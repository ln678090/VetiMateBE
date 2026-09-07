package com.graduation.project.notification.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.zalo-reminder")
public record ZaloReminderProperties(
    @NotNull URI gatewayBaseUrl,
    @NotNull URI publicAppUrl,
    @Min(1) @Max(100) int dailyLimit,
    @NotNull Duration minimumInterval,
    @NotNull Duration httpTimeout,
    @NotNull ZoneId timezone) {}
