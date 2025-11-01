package com.notificationservice.config;

import io.github.cdimascio.dotenv.Dotenv;
import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.io.InputStream;
import java.lang.IllegalStateException;
import java.util.Properties;

/**
 * AppConfig: quản lý cấu hình toàn hệ thống (JMS + Mail)
 */
public class AppConfig {

    // 📨 JMS config
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private static Connection connection;
    private static Session session;

    // 📧 Mail config
    private static String smtpHost;
    private static String smtpPort;
    private static String smtpUser;
    private static String smtpPass;

    /** Khởi tạo toàn bộ cấu hình khi Tomcat start */
    public static void init() {
        initJms();
        loadMailConfig();
    }

    // ========= JMS =========
    private static void initJms() {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(USERNAME, PASSWORD, BROKER_URL);
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("✅ [AppConfig] ActiveMQ connection established.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Session getSession() {
        return session;
    }

    public static void closeJms() {
        try {
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("🛑 [AppConfig] ActiveMQ connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========= MAIL =========
    private static void loadMailConfig() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("E:/CodeGym/Module3_C04/notification-service") // ⚙️ chỉnh path nếu khác
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

            if (smtpUser == null || smtpPass == null)
                throw new IllegalStateException("❌ Missing MAIL_USERNAME or MAIL_PASSWORD");

            System.out.println("✅ [AppConfig] Mail config loaded for user: " + smtpUser);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSmtpHost() { return smtpHost; }
    public static String getSmtpPort() { return smtpPort; }
    public static String getSmtpUser() { return smtpUser; }
    public static String getSmtpPass() { return smtpPass; }
}
