package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.QueueTicket;
import com.graduation.project.clinic.entity.QueueType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueTicketRepository extends JpaRepository<QueueTicket, UUID> {

  List<QueueTicket> findByQueueDateAndQueueTypeOrderByTicketNumberAsc(
      LocalDate queueDate, QueueType queueType);

  @Query(
      "SELECT MAX(q.ticketNumber) FROM QueueTicket q WHERE q.queueDate = :queueDate AND q.queueType = :queueType")
  Optional<Integer> findMaxTicketNumberByDateAndType(
      @Param("queueDate") LocalDate queueDate, @Param("queueType") QueueType queueType);
}
