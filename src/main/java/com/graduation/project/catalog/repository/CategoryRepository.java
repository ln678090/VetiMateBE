package com.graduation.project.catalog.repository;

import com.graduation.project.catalog.entity.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findBySlug(String slug);

  boolean existsBySlug(String slug);

  /** Lấy tất cả category root (parent_id IS NULL) đang active, sort theo sortOrder */
  @Query(
      """
            SELECT c FROM Category c
            WHERE c.parent IS NULL AND c.isActive = true
            ORDER BY c.sortOrder ASC, c.name ASC
            """)
  List<Category> findAllRootActive();

  /** Lấy children theo parentId */
  @Query(
      """
            SELECT c FROM Category c
            WHERE c.parent.id = :parentId AND c.isActive = true
            ORDER BY c.sortOrder ASC, c.name ASC
            """)
  List<Category> findChildrenByParentId(UUID parentId);

  /** Lấy toàn bộ category active (1 query duy nhất - dùng cho build tree in-memory) */
  @Query(
      """
            SELECT c FROM Category c
            WHERE c.isActive = true
            ORDER BY c.sortOrder ASC, c.name ASC
            """)
  List<Category> findAllActive();
}
