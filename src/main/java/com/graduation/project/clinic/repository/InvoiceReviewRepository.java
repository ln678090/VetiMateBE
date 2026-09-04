package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.entity.InvoiceReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceReviewRepository extends JpaRepository<InvoiceReview, UUID> {
    List<InvoiceReview> findByProduct_SlugOrderByCreatedAtDesc(String slug);
}
