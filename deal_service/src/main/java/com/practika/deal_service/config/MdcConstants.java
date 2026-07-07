package com.practika.deal_service.config;

// Константы для ключей MDC.

public final class MdcConstants {
    private MdcConstants() {} // Утилитарный класс

    public static final String REQUEST_ID = "requestId";
    public static final String APPLICATION_ID = "applicationId";
    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_EMAIL = "clientEmail";
    public static final String HTTP_METHOD = "httpMethod";
    public static final String HTTP_PATH = "httpPath";
    public static final String OPERATION = "operation";

    // Для notification_service
    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String EMAIL_STATUS = "emailStatus";
}
