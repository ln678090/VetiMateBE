package com.graduation.project.staff.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StaffConflictException extends RuntimeException {

  public StaffConflictException(String message) {
    super(message);
  }
}
