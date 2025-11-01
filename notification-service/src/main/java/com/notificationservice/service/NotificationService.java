package com.notificationservice.service;

import com.notificationservice.dto.PaymentRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * NotificationService (Servlet thuần)
 * ------------------------------------
 * - Nhận PaymentRequest từ ActiveMQ
 * - Gọi sang bank-service để lấy email người nhận
 * - Gửi 2 email: người nhận + người gửi
 */
public class NotificationService {

    private final EmailService emailService = new EmailService();

    public void sendNotification(PaymentRequest request) {
        try {
            System.out.println("💌 [NotificationService] Processing payment: " + request.getPaymentId());

            // 1️⃣ Gọi sang bank-service để lấy email người nhận
            String toEmail = getReceiverEmail(request.getToAccountId());
            if (toEmail != null && !toEmail.isEmpty()) {
                emailService.sendMail(
                        toEmail,
                        "Xác nhận thanh toán #" + request.getPaymentId(),
                        "<p>Bạn đã nhận được <b>" + request.getAmount() + " "
                                + request.getCurrency() + "</b>.</p>"
                                + "<p>Cảm ơn bạn đã sử dụng dịch vụ!</p>"
                );
                System.out.println("✅ [NotificationService] Sent email to receiver: " + toEmail);
            }

            // 2️⃣ Gửi email cho người gửi
            if (request.getSenderEmail() != null && !request.getSenderEmail().isEmpty()) {
                emailService.sendMail(
                        request.getSenderEmail(),
                        "Thông báo trừ tiền #" + request.getPaymentId(),
                        "<p>Bạn đã chuyển thành công <b>" + request.getAmount() + " "
                                + request.getCurrency() + "</b> tới tài khoản #"
                                + request.getToAccountId() + ".</p>"
                                + "<p>Trân trọng, Ngân hàng ABC.</p>"
                );
                System.out.println("✅ [NotificationService] Sent email to sender: " + request.getSenderEmail());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ [NotificationService] Failed to send notifications: " + e.getMessage());
        }
    }

    /**
     * Gọi sang bank-service để lấy email người nhận (GET /api/account/{id}/email)
     */
    private String getReceiverEmail(Long accountId) {
        try {
            String urlStr = "http://localhost:8080/api/account/" + accountId + "/email"; // ⚙️ chỉnh lại nếu cổng khác
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    conn.disconnect();
                    return response.toString().replace("\"", ""); // vì response có thể là chuỗi JSON "abc@gmail.com"
                }
            } else {
                System.err.println("⚠️ [NotificationService] Bank service responded: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("❌ [NotificationService] Error calling bank-service: " + e.getMessage());
        }
        return null;
    }
}
