package com.graduation.project.audit.dto;

import com.graduation.project.audit.entity.AuditAction;
import java.util.UUID;

public record AuditLogEvent(String module, String tableName, UUID recordId, AuditAction action, Object oldData,
    Object newData, UUID actorId, String actorIdentifier, String description, String ipAddress, String userAgent) {
}
