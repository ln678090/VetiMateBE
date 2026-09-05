package com.graduation.project.user.repository;

import com.graduation.project.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);

  boolean existsByUsername(String username);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "roles")
  Optional<User> findByEmail(String email);

  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "roles")
  Page<User> findAll(Pageable pageable);

  @Query("""
      select user
      from User user
      where not exists (
          select role.id
          from user.roles role
          where role.name = 'ROLE_ADMIN'
      )
  """)
  @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "roles")
  Page<User> findNonAdminUsers(Pageable pageable);

  @Query(
      """
      select user
      from User user
      where user.enabled = true
        and not exists (
            select staff.id
            from Staff staff
            where staff.user.id = user.id
        )
        and not exists (
            select role.id
            from user.roles role
            where role.name = 'ROLE_ADMIN'
        )
        and (
            :keyword = ''
            or lower(user.fullName)
                like concat('%', :keyword, '%')
            or lower(user.username)
                like concat('%', :keyword, '%')
            or lower(user.email)
                like concat('%', :keyword, '%')
            or lower(coalesce(user.phone, ''))
                like concat('%', :keyword, '%')
        )
      """)
  Page<User> findEligibleForStaff(@Param("keyword") String keyword, Pageable pageable);
}
