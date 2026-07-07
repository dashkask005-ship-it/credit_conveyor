   # Кредитный конвейер

Мы вам представляем надежные и масштабируемые микросервисы кредитного конвейера - калькулятора и отправки уведомлений, 
созданные с помощью Spring Boot. Он обеспечивает возможности отправления клиентами заявок 
на оказание кредитной услуги, подбор доступных условий, которые выведет калькулятор, а также уведомления заявителей о результате .

## Особенности

- **Автоматический скоринг**:  расчет кредитного рейтинга на основе возраста, дохода, стажа и других параметров
- **Генерация офферов**: создание до 4 вариантов кредита с разными условиями (страховка, зарплатный клиент)
- **Микросервисная архитектура**: 2 независимых сервиса (deal-service и notification-service) для гибкости и масштабирования
- **Интеграция через OpenFeign**: синхронное взаимодействие между сервисами
- **Управление миграциями**: автоматическое обновление схемы БД через Liquibase
- **Документация API**: встроенный Swagger UI для тестирования и ознакомления с эндпоинтами

## Технологический стек

- **Framework**: Spring Boot 3.2.0
- **Язык**: Java 17
- **База данных**: PostgreSQL
- **Миграции**: Liquibase
- **Взаимодействие**: Spring Cloud OpenFeign
- **Маппинг**: MapStruct
- **Документация API**: SpringDoc OpenAPI
- **Сборка**: Maven
- **Дополнительно**: Lombok, Spring Mail (SMTP)

## Предварительные требования
- Java 17 или выше
- Maven 3.9+
- PostgreSQL 15+
- Учетная запись Gmail с паролем приложения для отправки уведомлений

## Архитектура проекта
Проект состоит из двух модулей:

- deal_service (Порт 8080): основной сервис, обрабатывающий заявки, 
выполняющий скоринг и управляющий кредитными предложениями.
- notification_service (Порт 8081): сервис уведомлений, 
отвечающий за отправку email-писем клиентам.

## Установка и настройка

### 1. Клонирование репозитория
```bash
git clone <repository-url>
cd credit-conveyor
```


### 2. Настройка переменных окружения
Создайте файл .env в корневой директории проекта (или укажите 
переменные в вашей IDE) со следующими параметрами:
```env
# Настройки базы данных
DB_URL=jdbc:postgresql://localhost:5432/credit_conveyor
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Настройки для notification_service
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# URL для связи между сервисами (по умолчанию)
NOTIFICATION_URL=http://localhost:8081
```

### 3. Настройка Dokcker-copmose
Установить Docker Desctop
После выполнить команду:
```bash
docker compose up --build;
```


### 4. Настройка Gmail App Password 
1. Включите двухфакторную аутентификацию в своем аккаунте Google.
2. Сгенерируйте пароль приложения: https://support.google.com/accounts/answer/185833
3. Используйте сгенерированный пароль в переменной MAIL_PASSWORD.

### 5. Сборка и запуск
```bash
# Сборка всего проекта
mvn clean install

# Запуск deal_service (из папки deal_service)
cd deal_service
mvn spring-boot:run

# Запуск notification_service (в отдельном терминале, из папки notification_service)
cd ../notification_service
mvn spring-boot:run

### 6. Build and Run

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

После запуска сервисы будут доступны по адресам:
- deal_service: http://localhost:8080
- notification_service: http://localhost:8081

Swagger UI будет доступен по адресам:
- http://localhost:8080/swagger-ui.html
- http://localhost:8081/swagger-ui.html

# API Эндпоинты
## Deal Service (Порт 8080)
### Подача заявки на кредит
```http
POST /api/application
Content-Type: application/json

{
    "firstName": "Иван",
    "lastName": "Петров",
    "email": "ivan@example.com",
    "phone": "+79991234567",
    "workExperience": 5,
    "monthlyIncome": 120000,
    "birthDate": "1990-05-15",
    "employmentType": "EMPLOYED",
    "amount": 1000000,
    "term": 24,
    "purpose": "Покупка автомобиля"
}
```

### Выбор кредитного предложения
```POST /api/offer/select
Content-Type: application/json

{
    "applicationId": 1,
    "offerId": 2
}
```
## Notification Service (Порт 8081)
### Отправка уведомления (вызывается из deal_service)
```http
POST /api/notification/send
Content-Type: application/json

{
    "email": "client@example.com",
    "subject": "Ваш кредит одобрен",
    "message": "Текст письма...",
    "clientName": "Иван Петров",
    "status": "APPROVED",
    "applicationId": 1
}
```

## Примеры ответов
### Успешная заявка (одобрена)
```json
{
    "id": 1,
    "amount": 1000000,
    "term": 24,
    "purpose": "Покупка автомобиля",
    "status": "APPROVED",
    "rate": 14.00,
    "monthlyPayment": 47923.45,
    "createdAt": "2024-01-15T10:30:00",
    "clientName": "Иван Петров",
    "clientEmail": "ivan@example.com",
    "message": "Кредит одобрен! Ставка: 14.0%"
}
```

### Заявка с отказом
```json
{
    "id": 2,
    "amount": 1000000,
    "term": 24,
    "purpose": "Покупка автомобиля",
    "status": "REJECTED",
    "rate": 0.00,
    "monthlyPayment": 0.00,
    "createdAt": "2024-01-15T10:35:00",
    "clientName": "Сергей Смирнов",
    "clientEmail": "sergey@example.com",
    "message": "Доход меньше 20 000 ₽"
}
```

## Логика кредитного скоринга
### Кредитные оценки
- Возраст: 21–65 лет
- Доход: минимум 20 000 ₽ 
- Стаж работы: минимум 3 года
- Платежеспособность: ежемесячный платеж не более 50% дохода

## Корректировка процентной ставки
- Возраст: до 25 лет (+2%), после 55 лет (+1%), иначе (-1%)
- Доход: до 50 000 ₽ (+3%), более 150 000 ₽ (-2%)
- Страховка: (-3% к ставке)
- Зарплатный клиент: (-1% к ставке)

## Генерация предложений
Для одобренной заявки создаются 4 варианта кредита, комбинирующих:
- Со страховкой / Без страховки
- Зарплатный клиент / Обычный клиент

## Управление миграциями

Проект использует Liquibase для управления схемой БД. 
Все изменения хранятся в папке deal_service/src/main/resources/db/changelog/:
- 01-create-clients-table.sql
- 02-create-applications-table.sql
- 03-create-credits-table.sql
- 04-create-offers-table.sql
- cumulative-changelog.yaml (точка входа)

При запуске приложения миграции применяются автоматически.

## Конфигурация
### Deal Service (application.properties)
```properties
server.port=8080
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/postgres}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.liquibase.change-log=classpath:db/changelog/cumulative-changelog.yaml
services.notification.url=${NOTIFICATION_URL:http://localhost:8081}
```

### Notification Service (application.properties)
```properties
server.port=8081
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

## Логирование
Для отладки настроено детальное логирование:
- com.practika.deal_service: DEBUG
- org.hibernate.SQL: DEBUG
- org.springframework.mail: DEBUG

## Тестирование
Для запуска тестов выполните:
```bash

mvn test

```
## Планы по развитию
- Асинхронная обработка через RabbitMQ/Kafka
- Кэширование скоринговых моделей
- Расширение критериев скоринга
- Интеграция с внешними кредитными историями
- Метрики и мониторинг (Prometheus + Grafana)