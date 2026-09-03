package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.InvoiceItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, UUID> {}
