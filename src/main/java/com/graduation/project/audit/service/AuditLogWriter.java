package com.graduation.project.audit.service;

import com.graduation.project.audit.dto.AuditLogEvent;

public interface AuditLogWriter {

  void record(AuditLogEvent event);
}
