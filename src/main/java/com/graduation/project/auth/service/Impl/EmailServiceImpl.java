package com.graduation.project.auth.service.Impl;

import com.graduation.project.auth.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:}")
  private String senderEmail;

  @Async
  @Override
  public void sendOtpEmail(String toEmail, String otp) {
    if (senderEmail == null || senderEmail.isBlank()) {
      log.warn("⚠️ [EMAIL_SERVICE] Chưa cấu hình MAIL_USERNAME / MAIL_PASSWORD trong .env.properties. Không gửi qua SMTP.");
      return;
    }

    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(senderEmail, "VetiMate - Chăm sóc thú cưng");
      helper.setTo(toEmail);
      helper.setSubject("🔐 [VetiMate] Mã xác thực OTP đặt lại mật khẩu: " + otp);

      String htmlContent = """
          <div style="font-family: Arial, sans-serif; max-width: 540px; margin: 0 auto; padding: 24px; border: 1px solid #f0f0f0; border-radius: 16px; background-color: #ffffff;">
            <div style="text-align: center; margin-bottom: 24px;">
              <h2 style="color: #e11d48; margin: 0; font-size: 24px;">🐾 VetiMate</h2>
              <p style="color: #71717a; font-size: 14px; margin-top: 4px;">Hệ thống Chăm sóc & Phòng khám Thú cưng</p>
            </div>
            <div style="background: linear-gradient(135deg, #fff1f2, #fffbeb); padding: 20px; border-radius: 12px; text-align: center; margin-bottom: 20px;">
              <p style="color: #27272a; font-size: 15px; margin-bottom: 12px;">Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản <strong>%s</strong>.</p>
              <p style="color: #52525b; font-size: 13px; margin-bottom: 16px;">Mã xác thực OTP của bạn là:</p>
              <div style="display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #e11d48; background-color: #ffffff; padding: 12px 24px; border-radius: 8px; border: 2px dashed #f43f5e;">
                %s
              </div>
              <p style="color: #e11d48; font-size: 13px; font-weight: 500; margin-top: 14px;">⏱️ Mã này có hiệu lực trong vòng 5 phút.</p>
            </div>
            <p style="color: #a1a1aa; font-size: 12px; text-align: center; line-height: 1.5;">
              Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này hoặc liên hệ hỗ trợ để bảo vệ tài khoản.<br/>
              Trân trọng,<br/><strong>Đội ngũ VetiMate</strong>
            </p>
          </div>
          """.formatted(toEmail, otp);

      helper.setText(htmlContent, true);
      mailSender.send(message);
      log.info("📧 [EMAIL_SENT_SUCCESS] Đã gửi email chứa OTP tới: {}", toEmail);

    } catch (Exception ex) {
      log.error("❌ [EMAIL_SENT_FAILED] Lỗi gửi email tới {}: {}", toEmail, ex.getMessage());
    }
  }
}
