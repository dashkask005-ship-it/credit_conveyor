package com.practika.deal_service.config;

import com.practika.deal_service.util.MdcUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Фильтр для установки MDC переменных для каждого HTTP запроса.
// Выполняется перед контроллером.
@Component
@Order(1)
@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try{
            // 1. Устанавливаем requestId для каждого запроса
            String requestId = MdcUtil.setRequestId();

            // 2. Добавляем информацию о HTTP запросе
            MDC.put(MdcConstants.HTTP_METHOD, request.getMethod());
            MDC.put(MdcConstants.HTTP_PATH, request.getRequestURI());

            // 3. Логируем начало запроса
            log.info("→ Входящий запрос: {} {} (ID: {})",
                    request.getMethod(), request.getRequestURI(), requestId);

            // 4. Добавляем requestId в заголовок ответа для клиента
            response.setHeader("X-Request-Id", requestId);

            // Продолжаем цепочку фильтров
            filterChain.doFilter(request, response);
        } finally {
            // Обязательно очищаем MDC после обработки запроса.
            log.debug("<- Запрос обработан, очистка MDC");
        }

    }
}
