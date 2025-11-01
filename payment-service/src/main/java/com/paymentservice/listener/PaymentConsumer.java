package com.paymentservice.listener;

import com.paymentservice.config.JmsConfig;
import com.paymentservice.dto.PaymentRequest;
import com.paymentservice.service.PaymentService;

import javax.jms.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;

public class PaymentConsumer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JmsConfig.init();
            Session session = JmsConfig.getSession();
            Destination queue = session.createQueue("payment-queue");
            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    try {
                        if (message instanceof BytesMessage) {
                            BytesMessage bm = (BytesMessage) message;
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = bm.readBytes(buffer)) != -1) {
                                bos.write(buffer, 0, len);
                            }

                            ObjectInputStream ois = new ObjectInputStream(
                                    new ByteArrayInputStream(bos.toByteArray()));
                            PaymentRequest req = (PaymentRequest) ois.readObject();

                            System.out.println("📩 [PaymentConsumer] Received from queue:");
                            System.out.println("- ID: " + req.getPaymentId());
                            System.out.println("- Amount: " + req.getAmount() + " " + req.getCurrency());
                            System.out.println("- Sender Email: " + req.getSenderEmail());

                            // ✅ Gọi lại PaymentService (giữ nguyên logic ban đầu)
                            PaymentService paymentService = new PaymentService();
                            paymentService.processPayment(req);
                        } else {
                            System.err.println("⚠️ Received unknown message type: " + message.getClass().getName());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("👂 [PaymentConsumer] Listening on queue: payment-queue");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JmsConfig.close();
    }
}
