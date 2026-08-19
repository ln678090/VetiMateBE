
package com.graduation.project.inventory.repository;

import com.graduation.project.inventory.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

  List<Medicine> findAllByActiveTrueOrderByNameAsc();

  Optional<Medicine> findByIdAndActiveTrue(UUID id);

  List<Medicine> findAllByIdInAndActiveTrue(
      Collection<UUID> ids);
}
