# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Копируем ВСЁ, что нужно
COPY pom.xml /app/
COPY deal_service/pom.xml /app/deal_service/
COPY notification_service/pom.xml /app/notification_service/
COPY notification_service/src /app/notification_service/src

# Собираем с -am
RUN mvn clean package -pl notification_service -am -DskipTests -B

# Этап 2: Запуск
FROM eclipse-temurin:17
WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=builder /app/notification_service/target/notification_service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]