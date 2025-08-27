package com.abc.user_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;


import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.verification-url}")
    private String verificationUrl; // ví dụ: http://localhost:8080/users/verify

    @Value("${app.mail.from:no-reply@abc.com}")
    private String fromAddress;

    @Value("${app.mail.reply-to:}")
    private String replyTo; // để trống cũng được

    public void sendVerificationEmailHtml(String to, String token) {
        try {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Missing token");
            }

            // Phải là full URL (http/https), nếu không fromHttpUrl sẽ ném IllegalArgumentException
            String link = UriComponentsBuilder.fromHttpUrl(verificationUrl)
                    .queryParam("token", token)
                    .toUriString();

            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(
                    mime,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setValidateAddresses(true);
            helper.setFrom(fromAddress, "ABC Support"); // tên hiển thị tuỳ chỉnh
            if (replyTo != null && !replyTo.isBlank()) helper.setReplyTo(replyTo);

            helper.setSubject("Verify your account");

            String html = """
                <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;line-height:1.6">
                  <p>Please verify your account:</p>
                  <p style="margin:20px 0">
                    <a href="%s" style="display:inline-block;padding:10px 16px;text-decoration:none;border-radius:6px;border:1px solid #0d6efd">
                      Verify Now
                    </a>
                  </p>
                  <p>If you didn't request this, you can ignore this email.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:16px 0">
                  <p style="color:#666">If the button doesn't work, copy this link into your browser:<br>
                    <span style="word-break:break-all">%s</span>
                  </p>
                </div>
                """.formatted(link, link);

            // HTML + fallback text
            helper.setText("Please verify your account: " + link, html);

            mailSender.send(mime);
        } catch (Exception e) {
            log.warn("Send verification mail failed: {}", e.getMessage(), e);
        }
    }
}