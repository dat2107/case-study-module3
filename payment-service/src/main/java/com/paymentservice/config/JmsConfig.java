package com.paymentservice.config;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JmsConfig {
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static Connection connection;
    private static Session session;

    public static void init() {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("✅ [PaymentService] Connected to ActiveMQ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Session getSession() {
        return session;
    }

    public static void close() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("🛑 [PaymentService] ActiveMQ connection closed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
