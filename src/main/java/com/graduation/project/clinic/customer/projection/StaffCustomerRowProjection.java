package com.graduation.project.clinic.customer.projection;

import java.time.Instant;
import java.util.UUID;

public interface StaffCustomerRowProjection {

  UUID getId();

  String getFullName();

  String getPhone();

  String getEmail();

  long getPetCount();

  String getLatestAppointmentStatus();

  Instant getLatestAppointmentAt();
}
