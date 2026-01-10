# Bank Card Management Service

Backend-сервис для управления банковскими картами.  
Проект разработан с использованием Spring Boot и предназначен для запуска в Docker-окружении.

---

## Основные технологии

- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA (Hibernate)**
- **Liquibase**
- **PostgreSQL**
- **Gradle**
- **Docker / Docker Compose**
- **OpenApi / Swagger**

---

## Архитектура проекта

Проект состоит из нескольких сервисов, запускаемых через **Docker Compose**:

- `app` — Spring Boot приложение
- `postgres` — база данных PostgreSQL

Все сервисы запускаются в одной Docker-сети и взаимодействуют по именам сервисов.

---

## Структура проекта

compose.yaml # Docker Compose конфигурация

Dockerfile # Сборка Spring Boot приложения

build.gradle # Gradle конфигурация

src/main/resources/application.yaml   # Файл с конфигурационными настройками проекта

src/main/resources/db.migration      # Liquibase миграции 

src/main/resources/db.migration/root-changelog.xml # Главный liquibase  changelog


---

## Конфигурация приложения

Все параметры подключения к базе данных **передаются через переменные окружения**.

Spring Boot использует стандартные переменные:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Файл `application.yaml` **не содержит захардкоженных значений** и предназначен для работы в Docker-среде и конфигурирования приложения.

---

## Запуск проекта через Docker (РЕКОМЕНДУЕТСЯ)

### Требования

- Docker Desktop 4+
- Docker Compose v2+

---

### Запуск

Из корневой директории проекта выполнить:

```bash
docker compose down -v
docker compose up --build
```
После успешного запуска:

Приложение будет доступно по адресу
http://localhost:8080

### После запуска приложения доступны:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI спецификация: http://localhost:8080/api-docs

PostgreSQL будет доступен на порту 5432

### **Важные архитектурные решения**:
- номер карты пользователя и баланс на ней маскируются * (изменяется в application.yaml) в логах и для администратора. Для User и стороннего сервиса номера карт и их баланс выдаются без маски.
- пароли пользователей кодируются
- созданы роли в бд (и view для таблиц) ради разраничения доступа. Задаются в create-db-roles.xml.
