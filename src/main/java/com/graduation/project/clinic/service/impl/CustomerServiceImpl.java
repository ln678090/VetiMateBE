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
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository customerRepository;
  private final CustomerMapper customerMapper;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CustomerDto getOrCreateForCurrentUser(UUID userId) {
    return customerRepository
        .findByUser_Id(userId)
        .map(customerMapper::toDto)
        .orElseGet(() -> createCustomerForUser(userId, null));
  }

  @Override
  @Transactional
  public CustomerDto create(CustomerRequest request) {
    UUID userId = request.userId();

    if (customerRepository.existsByUser_Id(userId)) {
      throw new IllegalStateException("Tài khoản đã có hồ sơ chủ pet");
    }

    return createCustomerForUser(userId, request.note());
  }

  @Override
  @Transactional
  public CustomerDto update(UUID customerId, CustomerRequest request) {
    Customer customer =
        customerRepository
            .findByIdWithUser(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ chủ pet"));

    UUID currentOwnerId = customer.getUser().getId();

    if (!currentOwnerId.equals(request.userId())) {
      throw new IllegalArgumentException("Không được thay đổi tài khoản sở hữu");
    }

    customer.setNote(normalize(request.note()));

    Customer savedCustomer = customerRepository.save(customer);

    return customerMapper.toDto(savedCustomer);
  }

  @Override
  public CustomerDto getById(UUID customerId) {
    Customer customer =
        customerRepository
            .findByIdWithUser(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ chủ pet"));

    return customerMapper.toDto(customer);
  }

  @Override
  public Page<CustomerDto> search(String keyword, Pageable pageable) {
    String normalizedKeyword = keyword == null ? "" : keyword.trim();

    return customerRepository.search(normalizedKeyword, pageable).map(customerMapper::toDto);
  }

  @Override
  @Transactional
  public void delete(UUID customerId) {
    Customer customer =
        customerRepository
            .findByIdWithUser(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ chủ pet"));

    if (!customer.getPets().isEmpty()) {
      throw new IllegalStateException("Không thể xóa chủ pet đang có thú cưng");
    }

    /*
     * Nếu bảng appointment cũng liên kết Customer,
     * database sẽ từ chối xóa khi còn lịch sử.
     */
    customerRepository.delete(customer);
  }

  private CustomerDto createCustomerForUser(UUID userId, String note) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));

    Customer customer = Customer.builder().user(user).note(normalize(note)).build();

    Customer savedCustomer = customerRepository.save(customer);

    return customerMapper.toDto(savedCustomer);
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String normalized = value.trim();

    return normalized.isEmpty() ? null : normalized;
  }
}
