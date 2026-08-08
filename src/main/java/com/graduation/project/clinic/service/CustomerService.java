package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.dto.req.CustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
  CustomerDto create(CustomerRequest request);

  CustomerDto update(UUID id, CustomerRequest request);

  CustomerDto getById(UUID id);

  Page<CustomerDto> search(String keyword, Pageable pageable);

  void delete(UUID id);

  CustomerDto getOrCreateForCurrentUser(UUID userId);
}
