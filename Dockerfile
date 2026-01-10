# --- Этап 1: Сборка  ---
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Копируем файлы проекта
COPY . .

# Даем права на выполнение gradle wrapper
RUN chmod +x gradlew

# Собираем JAR.
RUN ./gradlew bootJar --no-daemon -x test

# --- Этап 2: Запуск  ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем собранный JAR из этапа сборки
COPY --from=builder /app/build/libs/*.jar app.jar

# Порт
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]