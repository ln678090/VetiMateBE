package com.graduation.project.clinic.service;

import com.graduation.project.clinic.dto.QueueTicketDto;
import com.graduation.project.clinic.dto.req.QueueStatusUpdateRequest;
import com.graduation.project.clinic.dto.req.QueueTicketRequest;
import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.QueueStatus;
import com.graduation.project.clinic.entity.QueueTicket;
import com.graduation.project.clinic.entity.QueueType;
import com.graduation.project.clinic.repository.AppointmentRepository;
import com.graduation.project.clinic.repository.QueueTicketRepository;
import com.graduation.project.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueueTicketService {

  private final QueueTicketRepository queueTicketRepository;
  private final AppointmentRepository appointmentRepository;

  @Transactional(readOnly = true)
  public List<QueueTicketDto> getTodayQueue(QueueType type) {
    LocalDate today = LocalDate.now();
    List<QueueTicket> tickets =
        queueTicketRepository.findByQueueDateAndQueueTypeOrderByTicketNumberAsc(today, type);
    return tickets.stream().map(this::mapToDto).collect(Collectors.toList());
  }

  @Transactional
  public QueueTicketDto createTicket(QueueTicketRequest request) {
    LocalDate today = LocalDate.now();
    Integer maxNumber =
        queueTicketRepository
            .findMaxTicketNumberByDateAndType(today, request.queueType())
            .orElse(0);

    QueueTicket ticket = new QueueTicket();
    ticket.setQueueDate(today);
    ticket.setQueueType(request.queueType());
    ticket.setTicketNumber(maxNumber + 1);
    ticket.setStatus(QueueStatus.WAITING);

    if (request.appointmentId() != null) {
      Appointment appointment =
          appointmentRepository
              .findById(request.appointmentId())
              .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
      ticket.setAppointment(appointment);
    }

    QueueTicket saved = queueTicketRepository.save(ticket);
    return mapToDto(saved);
  }

  @Transactional
  public QueueTicketDto updateStatus(UUID id, QueueStatusUpdateRequest request) {
    QueueTicket ticket =
        queueTicketRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Queue ticket not found"));

    ticket.setStatus(request.status());

    if (request.status() == QueueStatus.CALLED) {
      ticket.setCalledAt(Instant.now());
    } else if (request.status() == QueueStatus.DONE) {
      ticket.setCompletedAt(Instant.now());
    }

    QueueTicket saved = queueTicketRepository.save(ticket);
    return mapToDto(saved);
  }

  private QueueTicketDto mapToDto(QueueTicket ticket) {
    String customerName = null;
    String petName = null;
    String serviceName = null;
    UUID appointmentId = null;

    if (ticket.getAppointment() != null) {
      Appointment apt = ticket.getAppointment();
      appointmentId = apt.getId();
      if (apt.getCustomer() != null) {
        customerName = apt.getCustomer().getFullName();
      }
      if (apt.getPet() != null) {
        petName = apt.getPet().getName();
      }
      if (apt.getService() != null) {
        serviceName = apt.getService().getName();
      }
    }

    return new QueueTicketDto(
        ticket.getId(),
        appointmentId,
        customerName,
        petName,
        serviceName,
        ticket.getQueueDate(),
        ticket.getQueueType(),
        ticket.getTicketNumber(),
        ticket.getStatus(),
        ticket.getCalledAt(),
        ticket.getCompletedAt(),
        ticket.getCreatedAt());
  }
}
