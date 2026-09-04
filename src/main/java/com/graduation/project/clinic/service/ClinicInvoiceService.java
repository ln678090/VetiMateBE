package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.ClinicInvoiceDto;
import com.graduation.project.clinic.dto.req.CreateClinicInvoiceRequest;
import com.graduation.project.clinic.dto.req.PayClinicInvoiceRequest;
import java.util.List;
import java.util.UUID;

public interface ClinicInvoiceService {
  List<ClinicInvoiceDto> getAllInvoices();

  ClinicInvoiceDto getInvoiceById(UUID id);

  ClinicInvoiceDto createInvoice(CreateClinicInvoiceRequest request, UUID staffId);

  ClinicInvoiceDto payInvoice(UUID id, PayClinicInvoiceRequest request);

  ClinicInvoiceDto cancelInvoice(UUID id);
}
