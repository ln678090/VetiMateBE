package com.graduation.project.clinic.repository.specification;

import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Pet;
import com.graduation.project.clinic.entity.PetSpecies;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class PetSpecification {

  private PetSpecification() {}

  public static Specification<Pet> managementFilter(
      String keyword, PetSpecies species, Boolean deleted, UUID customerId) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      Join<Pet, Customer> customer = root.join("customer", JoinType.INNER);

      if (keyword != null && !keyword.isBlank()) {
        String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";

        predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("breed")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(customer.get("fullName")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(customer.get("phone")), pattern),
                criteriaBuilder.like(criteriaBuilder.lower(customer.get("email")), pattern)));
      }

      if (species != null) {
        predicates.add(criteriaBuilder.equal(root.get("species"), species));
      }

      if (customerId != null) {
        predicates.add(criteriaBuilder.equal(customer.get("id"), customerId));
      }

      if (Boolean.TRUE.equals(deleted)) {
        predicates.add(criteriaBuilder.isNotNull(root.get("deletedAt")));
      } else if (Boolean.FALSE.equals(deleted)) {
        predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
      }

      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }
}
