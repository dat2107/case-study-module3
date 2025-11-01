package com.notificationservice.listener;

import com.notificationservice.config.AppConfig;
import com.notificationservice.dto.PaymentRequest;
import com.notificationservice.service.NotificationService;

import javax.jms.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.*;

public class NotificationConsumer implements ServletContextListener {

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

                            System.out.println("📩 [NotificationConsumer] Received: " + req.getPaymentId());
                            new NotificationService().sendNotification(req);
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
