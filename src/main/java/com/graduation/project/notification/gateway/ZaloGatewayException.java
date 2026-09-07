package com.graduation.project.notification.gateway;

public class ZaloGatewayException extends RuntimeException {

  private final int statusCode;

  public ZaloGatewayException(int statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
  }

  public ZaloGatewayException(int statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public boolean isRetryable() {
    return statusCode == 0 || statusCode == 408 || statusCode == 429 || statusCode >= 500;
  }
}
