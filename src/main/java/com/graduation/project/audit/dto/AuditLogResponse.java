package com.graduation.project.audit.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.graduation.project.audit.entity.AuditAction;
import com.graduation.project.audit.entity.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
    UUID id,
    String module,
    String tableName,
    UUID recordId,
    AuditAction action,
    JsonNode oldData,
    JsonNode newData,
    UUID createdBy,
    String actorIdentifier,
    String description,
    String ipAddress,
    String userAgent,
    Instant createdAt) {

  public static AuditLogResponse from(AuditLog entity) {
    return new AuditLogResponse(
        entity.getId(),
        entity.getModule(),
        entity.getTableName(),
        entity.getRecordId(),
        entity.getAction(),
        entity.getOldData(),
        entity.getNewData(),
        entity.getCreatedBy(),
        entity.getActorIdentifier(),
        entity.getDescription(),
        entity.getIpAddress(),
        entity.getUserAgent(),
        entity.getCreatedAt());
  }
}
