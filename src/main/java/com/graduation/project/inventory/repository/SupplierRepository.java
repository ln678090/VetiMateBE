package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.Supplier;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

  List<Supplier> findByIsActiveTrueOrderByNameAsc();

  boolean existsByPhone(String phone);

  boolean existsByEmail(String email);
}
