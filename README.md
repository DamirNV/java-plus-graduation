```markdown
# Explore With Me

Backend-сервис для публикации событий, подбора мероприятий и управления заявками на участие.

Проект реализует REST API для основной платформы событий и отдельного сервиса статистики просмотров.

---

## 👥 Команда проекта

**Групповой учебный проект.** Команда из 4 разработчиков.

| Участник | Роль | Вклад |
|----------|------|-------|
| **Я (Damir)** | Участник А | • Административный API (категории, пользователи, подборки)<br>• Docker-инфраструктура и docker-compose<br>• Схема БД (schema.sql) и JPA-сущности<br>• Единый обработчик ошибок ErrorHandler<br>• **Фича "Комментарии к событиям"** — модель данных, репозиторий, DTO, мапперы<br>• Оптимизация: устранение проблемы N+1 запросов при выводе событий с комментариями |
| Коллега 1 | Участник Б | Публичный API, интеграция с сервисом статистики, сортировка по просмотрам |
| Коллега 2 | Участник В | Приватный API (события и заявки), модерация событий администратором |
| Коллега 3 | Участник Г | Административный API для комментариев, Postman-тесты |

> 📌 Мой код в этом репозитории: `AdminCategoryController`, `AdminUserController`, `AdminCompilationController`, `Comment`, `CommentStatus`, `CommentRepository`, `CommentMapper`, `ErrorHandler`, `schema.sql`, `docker-compose.yml`, а также оптимизация `countByEventIdsAndStatus` для массовой загрузки комментариев.

---

## Стек

- Java 21
- Spring Boot 3.3.0
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Docker / Docker Compose
- Checkstyle
- SpotBugs
- JaCoCo

## Описание проекта

Explore With Me позволяет пользователям создавать события, подавать заявки на участие, модерировать публикации и
получать подборки мероприятий.

Проект разделён на несколько логических частей:

- публичный API для просмотра опубликованных событий;
- приватный API для пользователей;
- административный API для модерации и управления справочниками;
- сервис статистики для сбора просмотров.

## Основные возможности

- Создание и редактирование событий
- Публикация и отклонение событий администратором
- Поиск событий по параметрам
- Управление категориями событий
- Создание подборок событий
- Подача и обработка заявок на участие
- Подтверждение или отклонение заявок инициатором события
- **Комментарии к событиям** (с модерацией)
- Сбор статистики просмотров
- Получение аналитики по посещаемости

## Архитектура

Проект построен как многомодульное Maven-приложение.

Основные слои приложения:

- `controller` — REST API
- `service` — бизнес-логика
- `repository` — доступ к данным
- `model` — JPA-сущности
- `dto` — входные и выходные модели API
- `mapper` — преобразование между DTO и entity
- `exception` — обработка ошибок

## Основные сущности

- `User` — пользователь системы
- `Event` — событие
- `Category` — категория события
- `Compilation` — подборка событий
- `ParticipationRequest` — заявка на участие
- `Comment` — комментарий к событию (статусы: PENDING, PUBLISHED, REJECTED, DELETED)
- `EndpointHit` — запись статистики обращения к endpoint

## Примеры API

### Публичный API

```http
GET /events
GET /events/{id}
GET /categories
GET /categories/{catId}
GET /compilations
GET /compilations/{compId}
GET /events/{eventId}/comments
GET /events/{eventId}/comments/{commentId}
```

### Приватный API

```http
POST /users/{userId}/events
PATCH /users/{userId}/events/{eventId}
GET /users/{userId}/events
POST /users/{userId}/requests
PATCH /users/{userId}/requests/{requestId}/cancel
POST /users/{userId}/events/{eventId}/comments
PATCH /users/{userId}/comments/{commentId}
DELETE /users/{userId}/comments/{commentId}
```

### Административный API

```http
POST /admin/categories
PATCH /admin/categories/{catId}
DELETE /admin/categories/{catId}

POST /admin/users
GET /admin/users
DELETE /admin/users/{userId}

PATCH /admin/events/{eventId}
POST /admin/compilations
PATCH /admin/compilations/{compId}
DELETE /admin/compilations/{compId}

GET /admin/comments
PATCH /admin/comments/{commentId}/publish
PATCH /admin/comments/{commentId}/reject
DELETE /admin/comments/{commentId}
```

### Сервис статистики

```http
POST /hit
GET /stats
```

## Запуск проекта

### Через Maven

```bash
mvn clean package
```

### Через Docker Compose

```bash
docker-compose up --build
```

## Проверка качества кода

В проекте настроены инструменты статического анализа и проверки качества:

```bash
mvn clean test
mvn clean package -P check
mvn clean verify -P coverage
```

Используются:

- Checkstyle
- SpotBugs
- JaCoCo

## Что демонстрирует проект

- разработку многомодульного Spring Boot backend-приложения;
- проектирование REST API;
- работу с PostgreSQL и Hibernate;
- разделение приложения на публичный, приватный и административный API;
- реализацию бизнес-логики модерации событий и заявок;
- **реализацию комментариев к событиям с модерацией (PENDING → PUBLISHED/REJECTED);**
- взаимодействие основного сервиса со статистическим сервисом;
- Docker-контейнеризацию;
- настройку проверки качества кода через Checkstyle, SpotBugs и JaCoCo.

---

## 📈 Мои ключевые задачи и достижения

| Задача | Решение |
|--------|---------|
| Административный API | Реализовал CRUD операций для категорий, пользователей и подборок событий |
| Docker-инфраструктура | Настроил контейнеризацию для микросервисов, stats-server и PostgreSQL |
| Схема БД | Спроектировал и написал schema.sql для всех сущностей с индексами |
| Обработка ошибок | Создал единый ErrorHandler с корректными статусами и форматом ApiError |
| Комментарии к событиям | Разработал полную модель данных, репозиторий, DTO и мапперы |
| Оптимизация N+1 | Добавил метод `countByEventIdsAndStatus` для массовой загрузки количества комментариев |

---
