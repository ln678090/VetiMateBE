package com.graduation.project.loyalty.repository;

import com.graduation.project.loyalty.entity.UserLoyaltyPoints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserLoyaltyPointsRepository extends JpaRepository<UserLoyaltyPoints, UUID> {
}
