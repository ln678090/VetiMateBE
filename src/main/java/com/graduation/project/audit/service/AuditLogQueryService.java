package com.graduation.project.audit.service;

import com.graduation.project.audit.dto.AuditLogResponse;
import com.graduation.project.audit.entity.AuditAction;
import com.graduation.project.audit.entity.AuditLog;
import com.graduation.project.audit.repository.AuditLogRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogQueryService {
  private final AuditLogRepository auditLogRepository;

  @Transactional(readOnly = true)
  public Page<AuditLogResponse> search(
      String actor,
      UUID createdBy,
      String module,
      AuditAction action,
      Instant from,
      Instant to,
      int page,
      int size) {
    Pageable pageable = PageRequest.of(
        page,
        Math.min(size, 100),
        Sort.by(Sort.Direction.DESC, "createdAt"));
    Specification<AuditLog> specification = buildSpecification(
        actor,
        createdBy,
        module,
        action,
        from,
        to);
    return auditLogRepository.findAll(specification, pageable).map(AuditLogResponse::from);
  }

  private Specification<AuditLog> buildSpecification(
      String actor,
      UUID createdBy,
      String module,
      AuditAction action,
      Instant from,
      Instant to) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (actor != null && !actor.isBlank()) {
        String pattern = "%" + actor.trim().toLowerCase(Locale.ROOT) + "%";
        predicates.add(
            criteriaBuilder.like(
                criteriaBuilder.lower(
                    root.get("actorIdentifier")),
                pattern));
      }

      if (createdBy != null) {
        predicates.add(
            criteriaBuilder.equal(
                root.get("createdBy"),
                createdBy));
      }

      if (module != null && !module.isBlank()) {
        predicates.add(
            criteriaBuilder.equal(
                root.get("module"),
                module.trim().toUpperCase(Locale.ROOT)));
      }

      if (action != null) {
        predicates.add(
            criteriaBuilder.equal(
                root.get("action"),
                action));
      }

      if (from != null) {
        predicates.add(
            criteriaBuilder.greaterThanOrEqualTo(
                root.get("createdAt"),
                from));
      }

      if (to != null) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(
                root.get("createdAt"),
                to));
      }

      return criteriaBuilder.and(
          predicates.toArray(new Predicate[0]));
    };
  }
}
