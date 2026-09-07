package com.graduation.project.clinic.customer.service;

import com.graduation.project.clinic.customer.dto.StaffCustomerFilter;
import com.graduation.project.clinic.customer.dto.StaffCustomerSummary;
import com.graduation.project.clinic.customer.projection.StaffCustomerRowProjection;
import com.graduation.project.clinic.repository.CustomerRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StaffCustomerQueryService {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 20;
  private static final int MAX_KEYWORD_LENGTH = 100;

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final CustomerRepository customerRepository;

  public Page<StaffCustomerSummary> search(
      String keyword, StaffCustomerFilter filter, Pageable pageable) {
    String safeKeyword = normalizeKeyword(keyword);

    StaffCustomerFilter safeFilter = filter == null ? StaffCustomerFilter.ALL : filter;

    Pageable safePageable = normalizePageable(pageable);

    Instant now = Instant.now();

    LocalDate currentBusinessDate = LocalDate.now(BUSINESS_ZONE);

    Instant dayStart = currentBusinessDate.atStartOfDay(BUSINESS_ZONE).toInstant();

    Instant dayEnd = currentBusinessDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

    return customerRepository
        .searchForStaff(safeKeyword, safeFilter.name(), now, dayStart, dayEnd, safePageable)
        .map(this::toSummary);
  }

  private String normalizeKeyword(String keyword) {
    if (keyword == null) {
      return "";
    }

    String normalized = keyword.trim();

    if (normalized.length() <= MAX_KEYWORD_LENGTH) {
      return normalized;
    }

    return normalized.substring(0, MAX_KEYWORD_LENGTH);
  }

  private Pageable normalizePageable(Pageable pageable) {
    if (pageable == null || pageable.isUnpaged()) {
      return PageRequest.of(0, DEFAULT_PAGE_SIZE);
    }

    int safePage = Math.max(pageable.getPageNumber(), 0);

    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

    return PageRequest.of(safePage, safeSize);
  }

  private StaffCustomerSummary toSummary(StaffCustomerRowProjection row) {
    return new StaffCustomerSummary(
        row.getId(),
        normalizeNullableText(row.getFullName()),
        normalizeNullableText(row.getPhone()),
        normalizeNullableText(row.getEmail()),
        row.getPetCount(),
        row.getLatestAppointmentStatus(),
        row.getLatestAppointmentAt());
  }

  private String normalizeNullableText(String value) {
    return value == null ? "" : value;
  }
}
