package com.graduation.project.loyalty.repository;

import com.graduation.project.loyalty.entity.UserLoyaltyPoints;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLoyaltyPointsRepository extends JpaRepository<UserLoyaltyPoints, UUID> {}
