package com.notificationservice.service;

import com.notificationservice.config.AppConfig;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    public void sendMail(String to, String subject, String htmlContent) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", AppConfig.getSmtpHost());
            prop.put("mail.smtp.port", AppConfig.getSmtpPort());
            prop.put("mail.smtp.ssl.trust", AppConfig.getSmtpHost());

            // 🔹 Đúng import class PasswordAuthentication
            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new javax.mail.PasswordAuthentication(
                            AppConfig.getSmtpUser(),
                            AppConfig.getSmtpPass()
                    );
                }
            });

            // 🔹 Tạo và gửi mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(AppConfig.getSmtpUser(), "Notification Service"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=UTF-8");

            // 🔹 Static method Transport.send()
            Transport.send(message);

            System.out.println("📧 Email sent successfully to " + to);

        } catch (AuthenticationFailedException e) {
            System.err.println("❌ Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}

