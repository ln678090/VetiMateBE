package com.graduation.project.clinic.repository;

import com.graduation.project.clinic.customer.projection.CustomerAccountIdentityProjection;
import com.graduation.project.clinic.customer.projection.StaffCustomerRowProjection;
import com.graduation.project.clinic.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository
    extends JpaRepository<Customer, UUID> {

  @EntityGraph(attributePaths = "user")
  Optional<Customer> findByUser_Id(UUID userId);

  boolean existsByUser_Id(UUID userId);

  @EntityGraph(attributePaths = "user")
  @Query("""
      SELECT customer
      FROM Customer customer
      WHERE customer.id = :customerId
      """)
  Optional<Customer> findByIdWithUser(
      @Param("customerId") UUID customerId);

  @EntityGraph(attributePaths = "user")
  @Query("""
      SELECT customer
      FROM Customer customer
      JOIN customer.user account
      WHERE (
          :keyword IS NULL
          OR :keyword = ''
          OR LOWER(COALESCE(account.fullName, ''))
              LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR COALESCE(account.phone, '')
              LIKE CONCAT('%', :keyword, '%')
          OR LOWER(COALESCE(account.email, ''))
              LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
      """)
  Page<Customer> search(
      @Param("keyword") String keyword,
      Pageable pageable);

  @Query(value = """
      SELECT
          customer.id AS "id",
          account.full_name AS "fullName",
          account.phone AS "phone",
          account.email AS "email",

          (
              SELECT COUNT(*)
              FROM clinic_pets pet
              WHERE pet.customer_id = customer.id
                AND pet.deleted_at IS NULL
          ) AS "petCount",

          (
              SELECT appointment.status
              FROM clinic_appointments appointment
              WHERE appointment.customer_id =
                    customer.id
              ORDER BY appointment.start_at DESC
              LIMIT 1
          ) AS "latestAppointmentStatus",

          (
              SELECT appointment.start_at
              FROM clinic_appointments appointment
              WHERE appointment.customer_id =
                    customer.id
              ORDER BY appointment.start_at DESC
              LIMIT 1
          ) AS "latestAppointmentAt"

      FROM clinic_customers customer

      JOIN public.users account
        ON account.id = customer.user_id

      WHERE EXISTS (
          SELECT 1
          FROM public.user_roles user_role
          JOIN public.roles role
            ON role.id = user_role.role_id
          WHERE user_role.user_id = account.id
            AND role.name = 'ROLE_USER'
      )

      AND NOT EXISTS (
          SELECT 1
          FROM public.user_roles other_user_role
          JOIN public.roles other_role
            ON other_role.id =
               other_user_role.role_id
          WHERE other_user_role.user_id =
                account.id
            AND other_role.name <> 'ROLE_USER'
      )

      AND (
          :keyword = ''

          OR LOWER(
              COALESCE(account.full_name, '')
          ) LIKE CONCAT(
              '%',
              LOWER(:keyword),
              '%'
          )

          OR COALESCE(account.phone, '')
              LIKE CONCAT('%', :keyword, '%')

          OR LOWER(
              COALESCE(account.email, '')
          ) LIKE CONCAT(
              '%',
              LOWER(:keyword),
              '%'
          )
      )

      AND (
          :filter = 'ALL'

          OR (
              :filter = 'TODAY'
              AND EXISTS (
                  SELECT 1
                  FROM clinic_appointments appointment
                  WHERE appointment.customer_id =
                        customer.id
                    AND appointment.start_at >=
                        :dayStart
                    AND appointment.start_at <
                        :dayEnd
              )
          )

          OR (
              :filter = 'UPCOMING'
              AND EXISTS (
                  SELECT 1
                  FROM clinic_appointments appointment
                  WHERE appointment.customer_id =
                        customer.id
                    AND appointment.start_at >= :now
                    AND appointment.status IN (
                        'SCHEDULED',
                        'CONFIRMED'
                    )
              )
          )

          OR (
              :filter = 'COMPLETED'
              AND EXISTS (
                  SELECT 1
                  FROM clinic_appointments appointment
                  WHERE appointment.customer_id =
                        customer.id
                    AND appointment.status = 'DONE'
              )
          )

          OR (
              :filter = 'NO_APPOINTMENT'
              AND NOT EXISTS (
                  SELECT 1
                  FROM clinic_appointments appointment
                  WHERE appointment.customer_id =
                        customer.id
              )
          )
      )

      ORDER BY account.full_name ASC
      """,

      countQuery = """
          SELECT COUNT(*)

          FROM clinic_customers customer

          JOIN public.users account
            ON account.id = customer.user_id

          WHERE EXISTS (
              SELECT 1
              FROM public.user_roles user_role
              JOIN public.roles role
                ON role.id = user_role.role_id
              WHERE user_role.user_id = account.id
                AND role.name = 'ROLE_USER'
          )

          AND NOT EXISTS (
              SELECT 1
              FROM public.user_roles other_user_role
              JOIN public.roles other_role
                ON other_role.id =
                   other_user_role.role_id
              WHERE other_user_role.user_id =
                    account.id
                AND other_role.name <> 'ROLE_USER'
          )

          AND (
              :keyword = ''

              OR LOWER(
                  COALESCE(account.full_name, '')
              ) LIKE CONCAT(
                  '%',
                  LOWER(:keyword),
                  '%'
              )

              OR COALESCE(account.phone, '')
                  LIKE CONCAT('%', :keyword, '%')

              OR LOWER(
                  COALESCE(account.email, '')
              ) LIKE CONCAT(
                  '%',
                  LOWER(:keyword),
                  '%'
              )
          )

          AND (
              :filter = 'ALL'

              OR (
                  :filter = 'TODAY'
                  AND EXISTS (
                      SELECT 1
                      FROM clinic_appointments appointment
                      WHERE appointment.customer_id =
                            customer.id
                        AND appointment.start_at >=
                            :dayStart
                        AND appointment.start_at <
                            :dayEnd
                  )
              )

              OR (
                  :filter = 'UPCOMING'
                  AND EXISTS (
                      SELECT 1
                      FROM clinic_appointments appointment
                      WHERE appointment.customer_id =
                            customer.id
                        AND appointment.start_at >= :now
                        AND appointment.status IN (
                            'SCHEDULED',
                            'CONFIRMED'
                        )
                  )
              )

              OR (
                  :filter = 'COMPLETED'
                  AND EXISTS (
                      SELECT 1
                      FROM clinic_appointments appointment
                      WHERE appointment.customer_id =
                            customer.id
                        AND appointment.status = 'DONE'
                  )
              )

              OR (
                  :filter = 'NO_APPOINTMENT'
                  AND NOT EXISTS (
                      SELECT 1
                      FROM clinic_appointments appointment
                      WHERE appointment.customer_id =
                            customer.id
                  )
              )
          )
          """, nativeQuery = true)
  Page<StaffCustomerRowProjection> searchForStaff(
      @Param("keyword") String keyword,
      @Param("filter") String filter,
      @Param("now") Instant now,
      @Param("dayStart") Instant dayStart,
      @Param("dayEnd") Instant dayEnd,
      Pageable pageable);

  @Query(value = """
      SELECT
          customer.id AS "customerId",
          account.full_name AS "fullName",
          account.phone AS "phone",
          account.email AS "email"
      FROM clinic_customers customer
      JOIN public.users account
        ON account.id = customer.user_id
      WHERE customer.id IN (:customerIds)
      """, nativeQuery = true)
  List<CustomerAccountIdentityProjection> findAccountIdentitiesByCustomerIds(
      @Param("customerIds") Collection<UUID> customerIds);
}
