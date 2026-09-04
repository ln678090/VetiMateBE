package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.ClinicServiceDto;
import com.graduation.project.clinic.dto.req.ClinicServiceRequest;
import com.graduation.project.clinic.entity.ClinicService;
import com.graduation.project.clinic.mapper.ClinicServiceMapper;
import com.graduation.project.clinic.repository.ClinicServiceRepository;
import com.graduation.project.clinic.service.ClinicServiceService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicServiceServiceImpl implements ClinicServiceService {

  private final ClinicServiceRepository clinicServiceRepository;
  private final ClinicServiceMapper clinicServiceMapper;

  @Override
  @Transactional
  public ClinicServiceDto create(ClinicServiceRequest request) {
    ClinicService service =
        ClinicService.builder()
            .name(request.name())
            .description(request.description())
            .price(request.price())
            .durationMin(request.durationMin())
            .isActive(request.isActive() == null || request.isActive()) // default true
            .build();
    return clinicServiceMapper.toDto(clinicServiceRepository.save(service));
  }

  @Override
  @Transactional
  public ClinicServiceDto update(UUID id, ClinicServiceRequest request) {
    ClinicService service =
        clinicServiceRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ: " + id));
    service.setName(request.name());
    service.setDescription(request.description());
    service.setPrice(request.price());
    service.setDurationMin(request.durationMin());
    if (request.isActive() != null) {
      service.setIsActive(request.isActive());
    }
    return clinicServiceMapper.toDto(clinicServiceRepository.save(service));
  }

  @Override
  @Transactional(readOnly = true)
  public ClinicServiceDto getById(UUID id) {
    ClinicService service =
        clinicServiceRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ: " + id));
    return clinicServiceMapper.toDto(service);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ClinicServiceDto> list(boolean activeOnly, Pageable pageable) {
    Page<ClinicService> page =
        activeOnly
            ? clinicServiceRepository.findByIsActiveTrue(pageable)
            : clinicServiceRepository.findAll(pageable);
    return page.map(clinicServiceMapper::toDto);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    if (!clinicServiceRepository.existsById(id)) {
      throw new IllegalArgumentException("Không tìm thấy dịch vụ: " + id);
    }
    clinicServiceRepository.deleteById(id);
  }
}
