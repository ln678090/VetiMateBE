package com.graduation.project.clinic.customer.projection;

import java.util.UUID;

public interface CustomerAccountIdentityProjection {

  UUID getCustomerId();

  String getFullName();

  String getPhone();

  String getEmail();
}
