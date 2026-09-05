package com.graduation.project.auth.service;

public interface EmailService {
  void sendOtpEmail(String toEmail, String otp);
}
