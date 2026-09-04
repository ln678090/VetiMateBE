package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.CustomerDto;
import com.graduation.project.clinic.dto.req.CustomerRequest;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.mapper.CustomerMapper;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.service.CustomerService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;

  private final CustomerMapper customerMapper;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CustomerDto getOrCreateForCurrentUser(UUID userId) {
    return customerRepository
        .findByUserId(userId)
        .map(customerMapper::toDto)
        .orElseGet(
            () -> {
              User user =
                  userRepository
                      .findById(userId)
                      .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

              Customer customer =
                  Customer.builder()
                      .userId(userId)
                      .fullName(user.getFullName())
                      .email(user.getEmail())
                      .phone("null")
                      .build();

              return customerMapper.toDto(customerRepository.save(customer));
            });
  }

  @Override
  @Transactional
  public CustomerDto create(CustomerRequest request) {
    if (request.phone() != null && customerRepository.existsByPhone(request.phone())) {
      throw new IllegalStateException("Số điện thoại đã tồn tại: " + request.phone());
    }
    Customer customer =
        Customer.builder()
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
    Customer customer =
        customerRepository
            .findById(id)
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
    Customer customer =
        customerRepository
            .findById(id)
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
