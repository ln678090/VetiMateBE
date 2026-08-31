package com.graduation.project.clinic.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "queue_tickets")
@Getter
@Setter
public class QueueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "queue_type", nullable = false, length = 20)
    private QueueType queueType;

    @Column(name = "ticket_number", nullable = false)
    private Integer ticketNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private QueueStatus status = QueueStatus.WAITING;

    @Column(name = "called_at")
    private Instant calledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
