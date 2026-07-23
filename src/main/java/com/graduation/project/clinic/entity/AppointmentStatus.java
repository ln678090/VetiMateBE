package com.graduation.project.clinic.entity;

public enum AppointmentStatus {
  SCHEDULED, // vừa đặt, chờ xác nhận
  CONFIRMED, // lễ tân/bác sĩ đã xác nhận
  DONE, // đã khám xong
  CANCELLED, // huỷ
  NO_SHOW // khách không đến
}
