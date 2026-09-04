package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.ClinicInvoiceDto;
import com.graduation.project.clinic.dto.ClinicInvoiceItemDto;
import com.graduation.project.clinic.dto.req.CreateClinicInvoiceRequest;
import com.graduation.project.clinic.dto.req.PayClinicInvoiceRequest;
import com.graduation.project.clinic.entity.ClinicService;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Invoice;
import com.graduation.project.clinic.entity.InvoiceItem;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.repository.ClinicServiceRepository;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.InvoiceRepository;
import com.graduation.project.clinic.repository.PetRepository;
import com.graduation.project.clinic.service.ClinicInvoiceService;
import com.graduation.project.inventory.entity.Medicine;
import com.graduation.project.inventory.repository.MedicineRepository;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.repository.StaffRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClinicInvoiceServiceImpl implements ClinicInvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;
  private final PetRepository petRepository;
  private final ClinicServiceRepository clinicServiceRepository;
  private final ProductRepository productRepository;
  private final MedicineRepository medicineRepository;
  private final StaffRepository staffRepository;

  @Override
  @Transactional(readOnly = true)
  public List<ClinicInvoiceDto> getAllInvoices() {
    return invoiceRepository.findAll().stream()
        .filter(inv -> !"SHOP".equals(inv.getType())) // Only CLINIC or MIXED
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(this::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public ClinicInvoiceDto getInvoiceById(UUID id) {
    Invoice invoice =
        invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));
    return mapToDto(invoice);
  }

  @Override
  @Transactional
  public ClinicInvoiceDto createInvoice(CreateClinicInvoiceRequest request, UUID staffId) {
    Customer customer =
        customerRepository
            .findById(request.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

    Pet pet = null;
    if (request.getPetId() != null) {
      pet =
          petRepository
              .findById(request.getPetId())
              .orElseThrow(() -> new RuntimeException("Pet not found"));
    }

    Staff staff = staffRepository.findByUserIdAndActiveTrue(staffId).orElse(null);

    Invoice invoice = new Invoice();
    invoice.setCustomer(customer);
    invoice.setPet(pet);
    invoice.setType("CLINIC");
    invoice.setStatus("PENDING"); // Pending payment
    invoice.setInvoiceCode("INV-" + System.currentTimeMillis());
    invoice.setNote(request.getNote());

    if (staff != null) {
      invoice.setCreatedBy(staff.getId());
    }

    BigDecimal total = BigDecimal.ZERO;
    List<InvoiceItem> items = new ArrayList<>();

    for (CreateClinicInvoiceRequest.ItemReq itemReq : request.getItems()) {
      InvoiceItem item = new InvoiceItem();
      item.setInvoice(invoice);
      item.setNameSnapshot(itemReq.getName());
      item.setQuantity(itemReq.getQuantity());
      item.setUnitPrice(itemReq.getUnitPrice());

      BigDecimal itemTotal = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
      item.setTotal(itemTotal);

      // Check reference
      if (itemReq.getServiceId() != null) {
        ClinicService service =
            clinicServiceRepository
                .findById(itemReq.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        item.setServiceId(service.getId());
      } else if (itemReq.getProductId() != null) {
        Product product =
            productRepository
                .findById(itemReq.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        item.setProduct(product);

        // Deduct stock for product
        if (product.getStockQuantity() < itemReq.getQuantity().intValue()) {
          throw new RuntimeException("Not enough stock for product: " + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity().intValue());
        productRepository.save(product);
      } else if (itemReq.getMedicineId() != null) {
        Medicine medicine =
            medicineRepository
                .findById(itemReq.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        item.setMedicineId(medicine.getId());
        // Medicine stock is managed by batches, skipping simple stock deduction here
      } else {
        throw new RuntimeException("Item must reference a service, product or medicine");
      }

      items.add(item);
      total = total.add(itemTotal);
    }

    invoice.setItems(items);
    invoice.setSubtotal(total);
    invoice.setDiscountAmount(BigDecimal.ZERO);
    invoice.setTotalAmount(total);

    invoice = invoiceRepository.save(invoice);
    return mapToDto(invoice);
  }

  @Override
  @Transactional
  public ClinicInvoiceDto payInvoice(UUID id, PayClinicInvoiceRequest request) {
    Invoice invoice =
        invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));

    if (!"PENDING".equals(invoice.getStatus()) && !"DRAFT".equals(invoice.getStatus())) {
      throw new RuntimeException("Only PENDING or DRAFT invoices can be paid");
    }

    invoice.setStatus("PAID");
    invoice.setPaymentMethod(request.getPaymentMethod());
    invoice.setPaidAt(Instant.now());

    invoice = invoiceRepository.save(invoice);
    return mapToDto(invoice);
  }

  @Override
  @Transactional
  public ClinicInvoiceDto cancelInvoice(UUID id) {
    Invoice invoice =
        invoiceRepository.findById(id).orElseThrow(() -> new RuntimeException("Invoice not found"));

    if ("PAID".equals(invoice.getStatus())) {
      throw new RuntimeException("Cannot cancel a paid invoice");
    }

    invoice.setStatus("CANCELLED");

    // Restore stock if product or medicine
    if (invoice.getItems() != null) {
      for (InvoiceItem item : invoice.getItems()) {
        if (item.getProduct() != null) {
          Product p = item.getProduct();
          p.setStockQuantity(p.getStockQuantity() + item.getQuantity().intValue());
          productRepository.save(p);
        } else if (item.getMedicineId() != null) {
          // Skip simple medicine stock restore
        }
      }
    }

    invoice = invoiceRepository.save(invoice);
    return mapToDto(invoice);
  }

  private ClinicInvoiceDto mapToDto(Invoice invoice) {
    List<ClinicInvoiceItemDto> itemDtos = new ArrayList<>();
    if (invoice.getItems() != null) {
      itemDtos =
          invoice.getItems().stream()
              .map(
                  item -> {
                    String type = "UNKNOWN";
                    if (item.getServiceId() != null) type = "SERVICE";
                    else if (item.getProduct() != null) type = "PRODUCT";
                    else if (item.getMedicineId() != null) type = "MEDICINE";

                    return ClinicInvoiceItemDto.builder()
                        .id(item.getId())
                        .name(item.getNameSnapshot())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .total(item.getTotal())
                        .type(type)
                        .build();
                  })
              .collect(Collectors.toList());
    }

    return ClinicInvoiceDto.builder()
        .id(invoice.getId())
        .invoiceCode(invoice.getInvoiceCode())
        .customerName(invoice.getCustomer() != null ? invoice.getCustomer().getFullName() : null)
        .customerPhone(invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : null)
        .petName(invoice.getPet() != null ? invoice.getPet().getName() : null)
        .type(invoice.getType())
        .status(invoice.getStatus())
        .subtotal(invoice.getSubtotal())
        .discountAmount(invoice.getDiscountAmount())
        .totalAmount(invoice.getTotalAmount())
        .paymentMethod(invoice.getPaymentMethod())
        .paidAt(invoice.getPaidAt())
        .note(invoice.getNote())
        .createdAt(invoice.getCreatedAt())
        .items(itemDtos)
        .build();
  }
}
