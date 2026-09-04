package com.graduation.project.staff.repository;

import com.graduation.project.staff.entity.Staff;
import com.graduation.project.staff.entity.StaffRoleType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StaffRepository extends JpaRepository<Staff, UUID> {
  Optional<Staff> findByUserIdAndActiveTrue(UUID userId);

  boolean existsByUserId(UUID userId);

  boolean existsByUserIdAndIdNot(UUID userId, UUID id);

  @Query(
      """
      select staff
      from Staff staff
      where (
          :keyword = ''
          or lower(staff.fullName) like concat('%', :keyword, '%')
          or lower(coalesce(staff.phone, '')) like concat('%', :keyword, '%')
          or lower(coalesce(staff.licenseNumber, '')) like concat('%', :keyword, '%')
      )
      and (
          :roleType is null
          or staff.roleType = :roleType
      )
      and (
          :active is null
          or staff.active = :active
      )
      """)
  Page<Staff> search(
      @Param("keyword") String keyword,
      @Param("roleType") StaffRoleType roleType,
      @Param("active") Boolean active,
      Pageable pageable);

  Optional<Staff> findByUserIdAndRoleTypeAndActiveTrue(UUID userId, StaffRoleType roleType);
}
