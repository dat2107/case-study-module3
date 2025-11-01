package com.paymentservice.service;

import com.paymentservice.config.JmsConfig;
import com.paymentservice.dto.PaymentRequest;

import javax.jms.*;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class PaymentService {

    public void processPayment(PaymentRequest paymentRequest) {
        System.out.println("💳 [PaymentService] Sending PaymentRequest to queue...");
        try {
            Session session = JmsConfig.getSession();
            if (session == null) {
                System.err.println("❌ JMS session not initialized.");
                return;
            }

            Destination destination = session.createQueue("payment-queue");
            MessageProducer producer = session.createProducer(destination);

            // serialize object to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(paymentRequest);
            oos.flush();

            BytesMessage message = session.createBytesMessage();
            message.writeBytes(bos.toByteArray());

            producer.send(message);
            System.out.println("📤 [PaymentService] PaymentRequest sent: " + paymentRequest.getPaymentId());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ [PaymentService] Failed to send message: " + e.getMessage());
        }
    }
}

