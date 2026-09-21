# Explore With Me

Backend-приложение для публикации событий, поиска мероприятий, управления заявками на участие и комментариями пользователей.

Проект реализован в микросервисной архитектуре на Java, Spring Boot и Spring Cloud.

## Стек технологий

- Java 21
- Spring Boot 3.3.0
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Spring Cloud Config
- Netflix Eureka
- Spring Cloud Gateway
- OpenFeign
- Spring Cloud LoadBalancer
- Maven
- Docker
- Docker Compose
- Checkstyle
- SpotBugs
- JaCoCo

## Возможности проекта

Explore With Me позволяет:

- создавать и редактировать события;
- публиковать и отклонять события;
- искать события по различным параметрам;
- управлять категориями событий;
- создавать подборки мероприятий;
- подавать заявки на участие;
- подтверждать и отклонять заявки;
- оставлять комментарии к событиям;
- модерировать комментарии;
- собирать статистику просмотров;
- получать информацию о количестве подтверждённых заявок и опубликованных комментариев.

## Архитектура

Приложение разделено на независимые Spring Boot микросервисы.

### Бизнес-сервисы

- `user-service` — управление пользователями;
- `event-service` — события, категории и подборки;
- `request-service` — заявки на участие в событиях;
- `comment-service` — комментарии и их модерация;
- `stats-server` — сбор и получение статистики обращений.

### Инфраструктурные сервисы

- `config-server` — централизованное хранение конфигурации;
- `discovery-server` — Service Discovery на базе Eureka;
- `gateway-server` — единая точка входа во внешнее API.

### Общий модуль

Модуль `common` содержит общие DTO, исключения и Feign-контракты, используемые для межсервисного взаимодействия.

Бизнес-сервисы не подключаются друг к другу как Maven-зависимости.

## Схема взаимодействия

```text
Client
  |
  v
Gateway
  |
  +------> user-service
  |
  +------> event-service
  |           |
  |           +------> user-service
  |           +------> request-service
  |           +------> comment-service
  |
  +------> request-service
  |           |
  |           +------> user-service
  |           +------> event-service
  |
  +------> comment-service
              |
              +------> user-service
              +------> event-service
```

Внешние запросы проходят через `gateway-server`.

Для взаимодействия между бизнес-сервисами используются:

- OpenFeign;
- Eureka Service Discovery;
- Spring Cloud LoadBalancer.

## Структура Maven-проекта

```text
java-plus-graduation
|
+-- core
|   |
|   +-- common
|   +-- user-service
|   +-- event-service
|   +-- request-service
|   +-- comment-service
|
+-- stats-service
|   |
|   +-- stats-dto
|   +-- stats-client
|   +-- stats-server
|
+-- infra
    |
    +-- config-server
    +-- discovery-server
    +-- gateway-server
```

Каждый бизнес-сервис сохраняет классическую слоистую структуру:

- `controller` — REST API;
- `service` — бизнес-логика;
- `repository` — доступ к данным;
- `model` — JPA-сущности;
- `dto` — входные и выходные модели;
- `mapper` — преобразование DTO и entity;
- `exception` — обработка ошибок.

## Хранение данных

Для бизнес-сервисов используется подход Database per Service.

| Сервис | База данных | Host port |
|---|---|---:|
| `stats-server` | `stats` | `6541` |
| `user-service` | `users` | `6543` |
| `request-service` | `requests` | `6544` |
| `event-service` | `events` | `6545` |
| `comment-service` | `comments` | `6546` |

Каждый сервис управляет собственной схемой данных.

Между таблицами разных сервисов нет внешних ключей.

Связи с сущностями других сервисов сохраняются в виде идентификаторов. Получение необходимых данных выполняется через межсервисные HTTP-вызовы.

Например:

- событие хранит `initiatorId`, а данные пользователя получает из `user-service`;
- заявка хранит `eventId` и `requesterId`;
- комментарий хранит `eventId` и `authorId`.

## Основные сущности

- `User` — пользователь;
- `Event` — событие;
- `Category` — категория события;
- `Compilation` — подборка событий;
- `Request` — заявка на участие;
- `Comment` — комментарий;
- `EndpointHit` — запись о запросе для сервиса статистики.

Комментарии поддерживают состояния:

- `PENDING`;
- `PUBLISHED`;
- `REJECTED`;
- `DELETED`.

## Service Discovery

Все сервисы регистрируются в Eureka.

Eureka Server доступен по адресу:

```text
http://localhost:8761
```

Бизнес-сервисы могут запускаться на динамических внутренних портах и находить друг друга по имени приложения.

Примеры имён сервисов:

```text
USER-SERVICE
EVENT-SERVICE
REQUEST-SERVICE
COMMENT-SERVICE
STATS-SERVER
GATEWAY-SERVER
CONFIG-SERVER
```

## Config Server

Конфигурация сервисов централизована в `config-server`.

Config Server доступен по адресу:

```text
http://localhost:8888
```

Пример получения конфигурации `event-service`:

```text
http://localhost:8888/event-service/default
```

В Config Server находятся конфигурации:

```text
user-service.properties
event-service.properties
request-service.properties
comment-service.properties
gateway-server.properties
```

## API Gateway

Внешнее API доступно через:

```text
http://localhost:8080
```

Gateway маршрутизирует запросы к соответствующим сервисам через Eureka и LoadBalancer.

Основные направления маршрутизации:

```text
/admin/users/**                         -> user-service

/users/*/requests/**                   -> request-service
/users/*/events/*/requests/**          -> request-service

/events/*/comments/**                  -> comment-service
/users/*/comments/**                   -> comment-service
/users/*/events/*/comments/**          -> comment-service
/admin/comments/**                     -> comment-service

/events/**                             -> event-service
/categories/**                         -> event-service
/compilations/**                       -> event-service
/users/*/events/**                     -> event-service
/admin/events/**                       -> event-service
/admin/categories/**                   -> event-service
/admin/compilations/**                 -> event-service
```

## Межсервисное взаимодействие

Feign-контракты находятся в модуле `common`.

Используются следующие клиенты:

### UserClient

Используется для получения информации о пользователях.

```text
user-service
/internal/users
```

### EventClient

Используется для получения информации о событиях.

```text
event-service
/internal/events
```

### RequestClient

Используется для получения информации о заявках и количестве подтверждённых заявок.

```text
request-service
/internal/requests
```

### CommentClient

Используется для получения количества опубликованных комментариев.

```text
comment-service
/internal/comments
```

## Отказоустойчивость межсервисных вызовов

Для OpenFeign настроены ограничения времени ожидания:

```properties
spring.cloud.openfeign.client.config.default.connectTimeout=1500
spring.cloud.openfeign.client.config.default.readTimeout=2500
```

Это позволяет сервисам быстро реагировать на недоступность зависимостей и не зависать на сетевых запросах.

Для некритичных агрегированных данных применяется graceful degradation.

Например, если `comment-service` временно недоступен, `event-service` продолжает возвращать данные события:

```json
{
  "comments": 0
}
```

Недоступность сервиса комментариев не делает получение события невозможным.

Критические бизнес-данные фиктивными значениями не подменяются.

Например, при недоступности `request-service` значение:

```text
confirmedRequests
```

не заменяется на `0`, поскольку это могло бы привести к неправильной интерпретации состояния события.

Такой межсервисный вызов завершается ошибкой за ограниченное время.

Автоматические retry для изменяющих состояние запросов не используются, чтобы исключить риск повторного выполнения операций `POST` и `PATCH`.

## Примеры API

### Публичный API событий

```http
GET /events
GET /events/{id}

GET /categories
GET /categories/{catId}

GET /compilations
GET /compilations/{compId}
```

### Пользовательский API событий

```http
POST /users/{userId}/events
GET /users/{userId}/events
GET /users/{userId}/events/{eventId}
PATCH /users/{userId}/events/{eventId}
```

### API заявок

```http
POST /users/{userId}/requests?eventId={eventId}
GET /users/{userId}/requests
PATCH /users/{userId}/requests/{requestId}/cancel
```

Также поддерживается обработка заявок инициатором события.

### API комментариев

Публичные запросы:

```http
GET /events/{eventId}/comments
GET /events/{eventId}/comments/{commentId}
```

Пользовательские запросы:

```http
POST /users/{userId}/events/{eventId}/comments
GET /users/{userId}/comments
PATCH /users/{userId}/comments/{commentId}
DELETE /users/{userId}/comments/{commentId}
```

Административные запросы:

```http
GET /admin/comments
PATCH /admin/comments/{commentId}/publish
PATCH /admin/comments/{commentId}/reject
DELETE /admin/comments/{commentId}
```

### Административный API пользователей

```http
POST /admin/users
GET /admin/users
DELETE /admin/users/{userId}
```

### Административный API категорий

```http
POST /admin/categories
PATCH /admin/categories/{catId}
DELETE /admin/categories/{catId}
```

### Административный API событий

```http
PATCH /admin/events/{eventId}
```

### Административный API подборок

```http
POST /admin/compilations
PATCH /admin/compilations/{compId}
DELETE /admin/compilations/{compId}
```

### Сервис статистики

```http
POST /hit
GET /stats
```

## Обработка ошибок

Каждый бизнес-сервис содержит собственный обработчик ошибок REST API.

Ошибки возвращаются в едином формате `ApiError`.

Обрабатываются, в частности:

- ошибки валидации;
- отсутствие сущности;
- конфликт бизнес-правил;
- некорректные параметры запроса;
- нарушения ограничений данных.

## Инфраструктурные порты

| Компонент | Порт |
|---|---:|
| API Gateway | `8080` |
| Eureka Server | `8761` |
| Config Server | `8888` |
| Stats PostgreSQL | `6541` |
| User PostgreSQL | `6543` |
| Request PostgreSQL | `6544` |
| Event PostgreSQL | `6545` |
| Comment PostgreSQL | `6546` |

## Запуск проекта

### Требования

Для запуска необходимы:

- Java 21;
- Maven;
- Docker;
- Docker Compose.

### Запуск через Docker Compose

Из корневой директории проекта:

```bash
docker compose up -d --build
```

Проверить состояние контейнеров:

```bash
docker compose ps
```

После запуска доступны:

```text
API Gateway:
http://localhost:8080

Eureka:
http://localhost:8761

Config Server:
http://localhost:8888
```

### Остановка

```bash
docker compose down
```

### Полная очистка данных

Для удаления контейнеров вместе с PostgreSQL volumes:

```bash
docker compose down -v
```

## Сборка и тестирование

Полная сборка Maven reactor:

```bash
mvn clean test
```

или:

```bash
mvn clean package
```

Проверки качества:

```bash
mvn clean package -P check
mvn clean verify -P coverage
```

Используются:

- Checkstyle;
- SpotBugs;
- JaCoCo.

## Проверка микросервисной архитектуры

Проект проверялся после полного удаления существующих PostgreSQL volumes:

```bash
docker compose down -v
docker compose up -d
```

После чистого запуска были проверены:

- регистрация сервисов в Eureka;
- получение конфигурации из Config Server;
- маршрутизация запросов через Gateway;
- создание пользователей;
- создание категории;
- создание события;
- публикация события;
- создание заявки;
- создание комментария;
- публикация комментария;
- получение публичных комментариев;
- межсервисное получение количества подтверждённых заявок;
- межсервисное получение количества опубликованных комментариев;
- отдельные PostgreSQL базы для сервисов;
- отсутствие таблиц других сервисов в базе `event-service`;
- отсутствие старого монолитного `ewm-service`.

Пример итогового состояния события:

```text
state             = PUBLISHED
confirmedRequests = 1
comments          = 1
```

## Проверка отказоустойчивости

Проверено поведение приложения при остановке зависимых сервисов.

При остановленном `comment-service`:

```text
event-service -> продолжает отвечать
comments      -> 0
```

После запуска `comment-service` значение автоматически восстанавливается.

При остановленном `request-service` получение события завершается ошибкой без подстановки некорректного значения `confirmedRequests = 0`.

В тестовой конфигурации ошибка при недоступном `request-service` возвращалась примерно за:

```text
1.66 sec
```

После восстановления сервиса:

```text
confirmedRequests = 1
comments          = 1
```

## Что демонстрирует проект

Проект демонстрирует:

- разработку микросервисной backend-системы на Spring Boot;
- построение многомодульного Maven-проекта;
- использование Spring Cloud;
- Service Discovery через Eureka;
- централизованную конфигурацию через Config Server;
- маршрутизацию API через Spring Cloud Gateway;
- межсервисное взаимодействие через OpenFeign;
- балансировку запросов через Spring Cloud LoadBalancer;
- подход Database per Service;
- работу с PostgreSQL и Hibernate;
- проектирование REST API;
- разделение публичного, пользовательского и административного API;
- реализацию бизнес-логики событий и заявок;
- модерацию комментариев;
- интеграцию со статистическим сервисом;
- graceful degradation для некритичных зависимостей;
- ограничение времени межсервисных вызовов;
- Docker-контейнеризацию всей системы;
- тестирование и статический анализ кода.
