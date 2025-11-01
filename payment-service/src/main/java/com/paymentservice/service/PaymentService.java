package com.paymentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentservice.config.JmsConfig;
import com.paymentservice.dto.PaymentRequest;

import javax.jms.*;

public class PaymentService {

    public void processPayment(PaymentRequest paymentRequest) {
        System.out.println("💳 [PaymentService] Sending PaymentRequest to queue (as JSON)...");
        try {
            Session session = JmsConfig.getSession();
            if (session == null) {
                System.err.println("❌ JMS session not initialized.");
                return;
            }

            Destination destination = session.createQueue("payment-queue");
            MessageProducer producer = session.createProducer(destination);

            // 🔹 Convert object to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(paymentRequest);

            // 🔹 Send as TextMessage
            TextMessage message = session.createTextMessage(json);
            producer.send(message);

            System.out.println("📤 [PaymentService] JSON sent to queue: " + json);

            // Cleanup
            producer.close();
            // ⚠️ session.close() sẽ được JmsConfig tự quản lý, không đóng ở đây để tránh shared session lỗi
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ [PaymentService] Failed to send message: " + e.getMessage());
        }
    }
}
