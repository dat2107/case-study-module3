package com.notificationservice.config;

import javax.jms.Session;

public class JmsConfig {
    public static Session getSession() {
        return AppConfig.getSession();
    }
}
