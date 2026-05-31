package com.graduation.project.catalog.repository;

import com.graduation.project.catalog.entity.Brand;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {

  Optional<Brand> findBySlug(String slug);

  boolean existsBySlug(String slug);

  @Query(
      """
            SELECT b FROM Brand b
            WHERE b.isActive = true
            ORDER BY b.name ASC
            """)
  List<Brand> findAllActive();
}
