package com.graduation.project.auth.service;

import com.graduation.project.staff.entity.StaffRoleType;
import java.util.UUID;

public interface RoleAssignmentService {
  void revokeStaffRoles(UUID targetUserId, UUID actorUserId, String reason);

  void assignStaffRole(UUID targetUserId, StaffRoleType roleType, UUID actorUserId, String reason);
}
