package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.dto.req.CustomerRequest;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.mapper.CustomerMapper;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;

  @Override
  @Transactional
  public CustomerDto getOrCreateForCurrentUser(UUID userId, String fullName) {
    // Idempotent: đã có -> trả về; chưa có -> tạo mới link user_id
    Customer customer = customerRepository.findByUserId(userId)
        .orElseGet(() -> {
          Customer created = Customer.builder()
              .userId(userId)
              .fullName(fullName)
              .build();
          return customerRepository.save(created);
        });
    return customerMapper.toDto(customer);
  }

  @Override
  @Transactional
  public CustomerDto create(CustomerRequest request) {
    if (request.phone() != null && customerRepository.existsByPhone(request.phone())) {
      throw new IllegalStateException("Số điện thoại đã tồn tại: " + request.phone());
    }
    Customer customer = Customer.builder()
        .fullName(request.fullName())
        .phone(request.phone())
        .email(request.email())
        .address(request.address())
        .build();
    return customerMapper.toDto(customerRepository.save(customer));
  }

  @Override
  @Transactional
  public CustomerDto update(UUID id, CustomerRequest request) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng: " + id));
    customer.setFullName(request.fullName());
    customer.setPhone(request.phone());
    customer.setEmail(request.email());
    customer.setAddress(request.address());
    return customerMapper.toDto(customerRepository.save(customer));
  }

  @Override
  @Transactional(readOnly = true)
  public CustomerDto getById(UUID id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng: " + id));
    return customerMapper.toDto(customer);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CustomerDto> search(String keyword, Pageable pageable) {
    return customerRepository.search(keyword, pageable).map(customerMapper::toDto);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!customerRepository.existsById(id)) {
      throw new IllegalArgumentException("Không tìm thấy khách hàng: " + id);
    }
    customerRepository.deleteById(id);
  }
}
