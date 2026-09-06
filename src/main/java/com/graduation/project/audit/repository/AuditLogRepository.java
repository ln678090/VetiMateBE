package com.graduation.project.audit.repository;

import com.graduation.project.audit.entity.AuditLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository
    extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {}
