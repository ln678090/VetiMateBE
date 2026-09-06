package com.graduation.project.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graduation.project.audit.dto.AuditLogEvent;
import com.graduation.project.audit.entity.AuditLog;
import com.graduation.project.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuditLogWriterImpl implements AuditLogWriter {

  private static final Set<String> SENSITIVE_FIELDS = Set.of(
      "password",
      "passwordhash",
      "accesstoken",
      "refreshtoken",
      "token",
      "otp",
      "otphash",
      "mfasecret",
      "authorization",
      "cookie",
      "secret");

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void record(AuditLogEvent event) {
    AuditLog auditLog = AuditLog.create(
        normalizeModule(event.module()),
        requireText(event.tableName(), "tableName"),
        event.recordId(),
        event.action(),
        sanitize(event.oldData()),
        sanitize(event.newData()),
        event.actorId(),
        truncate(event.actorIdentifier(), 255),
        truncate(event.description(), 500),
        truncate(event.ipAddress(), 45),
        truncate(event.userAgent(), 500));

    auditLogRepository.save(auditLog);
  }

  private JsonNode sanitize(Object source) {
    if (source == null) {
      return null;
    }

    JsonNode node = objectMapper.valueToTree(source);
    redactSensitiveFields(node);

    return node;
  }

  private void redactSensitiveFields(JsonNode node) {
    if (node == null) {
      return;
    }

    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();

        if (isSensitive(field.getKey())) {
          objectNode.put(field.getKey(), "[REDACTED]");
        } else {
          redactSensitiveFields(field.getValue());
        }
      }

      return;
    }

    if (node.isArray()) {
      node.forEach(this::redactSensitiveFields);
    }
  }

  private boolean isSensitive(String fieldName) {
    String normalized = fieldName
        .replace("_", "")
        .replace("-", "")
        .toLowerCase(Locale.ROOT);

    return SENSITIVE_FIELDS.contains(normalized);
  }

  private String normalizeModule(String module) {
    return requireText(module, "module")
        .toUpperCase(Locale.ROOT);
  }

  private String requireText(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(
          fieldName + " không được để trống");
    }

    return value.trim();
  }

  private String truncate(String value, int maximumLength) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();

    return trimmed.length() <= maximumLength
        ? trimmed
        : trimmed.substring(0, maximumLength);
  }
}
