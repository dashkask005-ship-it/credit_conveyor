package com.practika.deal_service.util;

import com.practika.deal_service.config.MdcConstants;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

/**
 * Утилитный класс для упрощения работы с MDC.
 * Предоставляет типизированные методы для частых операций.
 */
public final class MdcUtil {
    private MdcUtil() {}

    // Базовые методы для работы с MDC

    /**
     * Получить значение из MDC по ключу.
     * Использует константы из MdcConstants, но принимает любой String.
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    // Установить значение в MDC.

    public static void put(String key, String value) {
        MDC.put(key, value);
    }

    // Удалить значение из MDC.
    public static void remove(String key) {
        MDC.remove(key);
    }

    // Получить копию всего контекста MDC.
    public static Map<String, String> getCopyOfContextMap() {
        return MDC.getCopyOfContextMap();
    }

    // Очистить весь MDC.
    public static void clear() {
        MDC.clear();
    }

    // Специализированные методы для вашего проекта

    /**
     * Генерирует и устанавливает requestId.
     * @return сгенерированный requestId
     */
    public static String setRequestId() {
        String requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MdcConstants.REQUEST_ID, requestId);
        return requestId;
    }

    /**
     * Устанавливает ID заявки.
     */
    public static void setApplicationId(Long applicationId) {
        if (applicationId != null) {
            MDC.put(MdcConstants.APPLICATION_ID, String.valueOf(applicationId));
        }
    }

    /**
     * Получить ID заявки из MDC.
     */
    public static Long getApplicationId() {
        String id = MDC.get(MdcConstants.APPLICATION_ID);
        return id != null ? Long.parseLong(id) : null;
    }

    /**
     * Устанавливает ID клиента.
     */
    public static void setClientId(Long clientId) {
        if (clientId != null) {
            MDC.put(MdcConstants.CLIENT_ID, String.valueOf(clientId));
        }
    }

    /**
     * Получить ID клиента из MDC.
     */
    public static Long getClientId() {
        String id = MDC.get(MdcConstants.CLIENT_ID);
        return id != null ? Long.parseLong(id) : null;
    }

    /**
     * Устанавливает email клиента в MDC (с маскированием для безопасности).
     */
    public static void setClientEmail(String email) {
        if (email != null) {
            MDC.put(MdcConstants.CLIENT_EMAIL, maskEmail(email));
        }
    }

    /**
     * Получить email клиента из MDC.
     */
    public static String getClientEmail() {
        return MDC.get(MdcConstants.CLIENT_EMAIL);
    }

    /**
     * Устанавливает название текущей операции.
     */
    public static void setOperation(String operation) {
        MDC.put(MdcConstants.OPERATION, operation);
    }

    /**
     * Получить название текущей операции.
     */
    public static String getOperation() {
        return MDC.get(MdcConstants.OPERATION);
    }

    // Получить requestId из MDC.
    public static String getRequestId() {
        return MDC.get(MdcConstants.REQUEST_ID);
    }

    // Установить HTTP метод.
    public static void setHttpMethod(String method) {
        MDC.put(MdcConstants.HTTP_METHOD, method);
    }

    // Установить HTTP путь.
    public static void setHttpPath(String path) {
        MDC.put(MdcConstants.HTTP_PATH, path);
    }

    // Вспомогательные приватные методы

    /**
     * Маскирование email для логирования.
     * Пример: ivan@example.com → i***@example.com
     */
    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        if (name.length() <= 2) {
            return name + "@" + parts[1];
        }
        return name.charAt(0) + "***@" + parts[1];
    }
}