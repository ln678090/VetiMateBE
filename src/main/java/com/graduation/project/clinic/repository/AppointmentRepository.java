package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.Appointment;
import com.graduation.project.clinic.entity.AppointmentStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

  // JOIN FETCH đầy đủ customer + pet + service để flatten ra DTO (chống N+1)
  @Query("""
      SELECT a FROM Appointment a
      JOIN FETCH a.customer
      JOIN FETCH a.pet
      JOIN FETCH a.service
      WHERE a.id = :id
      """)
  Optional<Appointment> findByIdFull(@Param("id") UUID id);

  @Query(value = """
      SELECT a FROM Appointment a
      JOIN FETCH a.customer
      JOIN FETCH a.pet
      JOIN FETCH a.service
      WHERE a.customer.id = :customerId
      ORDER BY a.startAt DESC
      """, countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.customer.id = :customerId")
  Page<Appointment> findByCustomerIdFull(@Param("customerId") UUID customerId, Pageable pageable);

  // Chống trùng giờ: có lịch nào của cùng service giao khoảng [startAt, endAt)
  // không?
  // Loại trừ trạng thái CANCELLED. Overlap khi: existing.startAt < newEnd AND
  // existing.endAt > newStart
  @Query("""
      SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
      FROM Appointment a
      WHERE a.service.id = :serviceId
        AND a.status <> :cancelled
        AND a.startAt < :newEnd
        AND a.endAt   > :newStart
      """)
  boolean existsOverlap(@Param("serviceId") UUID serviceId,
      @Param("newStart") Instant newStart,
      @Param("newEnd") Instant newEnd,
      @Param("cancelled") AppointmentStatus cancelled);

  @Query("""
      SELECT a FROM Appointment a
      WHERE a.startAt < :dayEnd AND a.endAt > :dayStart
        AND a.status IN (
            com.graduation.project.clinic.entity.AppointmentStatus.SCHEDULED,
            com.graduation.project.clinic.entity.AppointmentStatus.CONFIRMED)
      """)
  List<Appointment> findActiveBetween(Instant dayStart, Instant dayEnd);

  @Query("SELECT a FROM Appointment a WHERE a.service.id = :serviceId " +
      "AND a.startAt >= :start AND a.startAt < :end " +
      "AND a.status NOT IN ('CANCELLED', 'NO_SHOW')")
  List<Appointment> findActiveByServiceAndDay(
      @Param("serviceId") UUID serviceId,
      @Param("start") Instant start, // ← Instant
      @Param("end") Instant end // ← Instant
  );

  // @Query(value = """
  // SELECT appointment
  // FROM Appointment appointment
  // JOIN FETCH appointment.customer
  // JOIN FETCH appointment.pet
  // JOIN FETCH appointment.service
  // WHERE appointment.startAt >= :startAt
  // AND appointment.startAt < :endAt
  // AND (:status IS NULL OR appointment.status = :status)
  // """, countQuery = """
  // SELECT COUNT(appointment)
  // FROM Appointment appointment
  // WHERE appointment.startAt >= :startAt
  // AND appointment.startAt < :endAt
  // AND (:status IS NULL OR appointment.status = :status)
  // """)
  // Page<Appointment> findForManagement(
  // @Param("startAt") Instant startAt,
  // @Param("endAt") Instant endAt,
  // @Param("status") AppointmentStatus status,
  // Pageable pageable);

  @Query(value = """
      SELECT appointment
      FROM Appointment appointment
      JOIN FETCH appointment.customer
      JOIN FETCH appointment.pet
      JOIN FETCH appointment.service
      WHERE appointment.startAt >= :startAt
        AND appointment.startAt < :endAt
        AND (:status IS NULL OR appointment.status = :status)
      """, countQuery = """
      SELECT COUNT(appointment)
      FROM Appointment appointment
      WHERE appointment.startAt >= :startAt
        AND appointment.startAt < :endAt
        AND (:status IS NULL OR appointment.status = :status)
      """)
  Page<Appointment> findForManagement(
      @Param("startAt") Instant startAt,
      @Param("endAt") Instant endAt,
      @Param("status") AppointmentStatus status,
      Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = { "customer", "pet", "service" })
  @Query("""
      SELECT appointment
      FROM Appointment appointment
      WHERE appointment.id = :appointmentId
      """)
  Optional<Appointment> findByIdForUpdate(
      @Param("appointmentId") UUID appointmentId);

}
