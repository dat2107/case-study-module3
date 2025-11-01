package com.bank.service;

import io.github.cdimascio.dotenv.Dotenv;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private String smtpHost;
    private String smtpPort;
    private String smtpUser;
    private String smtpPass;

    // ✅ Constructor: Tự động load config
    public EmailService() {
        loadConfig();
    }

    // ✅ Load config từ .env và application.properties
    private void loadConfig() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("E:/CodeGym/Module3_C04/bank") // ✅ thư mục chứa file .env của bạn
                    .ignoreIfMissing()
                    .load();


            InputStream input = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream("application.properties");

            Properties props = new Properties();
            if (input != null) props.load(input);

            smtpHost = props.getProperty("mail.smtp.host", "smtp.gmail.com");
            smtpPort = props.getProperty("mail.smtp.port", "587");
            smtpUser = dotenv.get("MAIL_USERNAME", props.getProperty("mail.smtp.user"));
            smtpPass = dotenv.get("MAIL_PASSWORD", props.getProperty("mail.smtp.password"));

            if (smtpUser == null || smtpPass == null) {
                throw new IllegalStateException("⚠️ Missing MAIL_USERNAME or MAIL_PASSWORD. Check your .env or properties file!");
            }

            System.out.println("✅ Loaded mail config from .env or application.properties");
            System.out.println("📧 SMTP USER: " + smtpUser);

        } catch (Exception e) {
            throw new RuntimeException("❌ Cannot load mail config", e);
        }
    }

    // ✅ Gửi email HTML
    public void sendEmail(String to, String subject, String content) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", smtpHost);
        prop.put("mail.smtp.port", smtpPort);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.ssl.trust", smtpHost);

        // 👉 Giúp in log kết nối SMTP rõ ràng hơn
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUser, smtpPass);
            }
        });
        session.setDebug(true); // bật log SMTP chi tiết

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUser, "Bank Notification"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=UTF-8");

            System.out.println("🚀 Sending mail to: " + to);
            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + to);

        } catch (AuthenticationFailedException authEx) {
            System.err.println("❌ Authentication failed: " + authEx.getMessage());
            System.err.println("👉 Kiểm tra lại App Password Gmail và file .env");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }
}
