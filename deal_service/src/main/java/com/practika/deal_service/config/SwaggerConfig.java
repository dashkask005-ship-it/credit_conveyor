package com.practika.deal_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🏦 Кредитный конвейер API")
                        .version("1.0.0")
                        .description("""
                    API для управления кредитным конвейером.
                    
                    **Функционал:**
                    - Создание заявки
                    - Расчет кредита
                    - Выбор предложения
                    - Отправка сообщения с решением на почту
                    """)
                );
    }
}