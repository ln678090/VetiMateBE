package com.graduation.project.audit.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Immutable
@Table(name = "audit_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

  @Id
  @Column(nullable = false, updatable = false)
  private UUID id;

  @Column(name = "table_name", nullable = false, length = 100)
  private String tableName;

  @Column(name = "record_id")
  private UUID recordId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AuditAction action;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "old_data", columnDefinition = "jsonb")
  private JsonNode oldData;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "new_data", columnDefinition = "jsonb")
  private JsonNode newData;

  @Column(name = "created_by")
  private UUID createdBy;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false, length = 50)
  private String module;

  @Column(name = "actor_identifier", length = 255)
  private String actorIdentifier;

  @Column(length = 500)
  private String description;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  public static AuditLog create(
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
      String userAgent) {
    AuditLog auditLog = new AuditLog();

    auditLog.id = UUID.randomUUID();
    auditLog.module = module;
    auditLog.tableName = tableName;
    auditLog.recordId = recordId;
    auditLog.action = action;
    auditLog.oldData = oldData;
    auditLog.newData = newData;
    auditLog.createdBy = createdBy;
    auditLog.createdAt = Instant.now();
    auditLog.actorIdentifier = actorIdentifier;
    auditLog.description = description;
    auditLog.ipAddress = ipAddress;
    auditLog.userAgent = userAgent;

    return auditLog;
  }
}
