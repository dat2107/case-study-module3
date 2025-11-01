package com.notificationservice.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.config.AppConfig;
import com.notificationservice.dto.PaymentRequest;
import com.notificationservice.service.NotificationService;

import javax.jms.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;

public class NotificationConsumer implements ServletContextListener {
    private final NotificationService notificationService = new NotificationService();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            AppConfig.init(); // Khởi tạo cả JMS & Mail config
            Session session = AppConfig.getSession();
            Destination queue = session.createQueue("payment-queue");
            MessageConsumer consumer = session.createConsumer(queue);

            consumer.setMessageListener(new MessageListener() {
                public void onMessage(Message message) {
                    try {
                        if (message instanceof TextMessage) {
                            TextMessage textMessage = (TextMessage) message;
                            String json = textMessage.getText();

                            System.out.println("📩 [NotificationConsumer] Received JSON: " + json);

                            ObjectMapper mapper = new ObjectMapper();
                            PaymentRequest req = mapper.readValue(json, PaymentRequest.class);

                            notificationService.sendNotification(req);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            System.out.println("👂 [NotificationService] Listening on queue: payment-queue");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AppConfig.closeJms();
    }
}
