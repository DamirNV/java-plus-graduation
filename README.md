# Explore With Me

Explore With Me — backend-приложение для публикации событий, поиска мероприятий, управления заявками на участие, комментариями и персональными рекомендациями.

Проект реализован как микросервисная система на Java, Spring Boot и Spring Cloud.

## Технологии

- Java 21
- Spring Boot 3.3
- Spring Data JPA
- PostgreSQL
- Spring Cloud Config
- Netflix Eureka
- Spring Cloud Gateway
- OpenFeign
- Spring Cloud LoadBalancer
- Resilience4j
- gRPC / Protocol Buffers
- Apache Kafka
- Apache Avro
- Maven
- Docker / Docker Compose
- Checkstyle
- SpotBugs
- JaCoCo

## Возможности

Приложение поддерживает:

- управление пользователями;
- создание, редактирование и публикацию событий;
- публичный поиск событий;
- категории и подборки событий;
- заявки на участие;
- подтверждение и отклонение заявок;
- комментарии и их модерацию;
- сбор пользовательских действий;
- расчёт рейтинга событий;
- расчёт сходства событий;
- персональные рекомендации;
- legacy REST API статистики для обратной совместимости.

## Архитектура

Система состоит из бизнес-сервисов, инфраструктурных сервисов и подсистемы рекомендаций.

~~~text
                              +------------------+
                              |      Client      |
                              +---------+--------+
                                        |
                                        v
                              +------------------+
                              |  Gateway Server  |
                              +---------+--------+
                                        |
            +---------------------------+---------------------------+
            |                           |                           |
            v                           v                           v
     +-------------+             +-------------+             +-------------+
     | user-service|             |event-service|             |request-     |
     +-------------+             +------+------+             |service      |
                                       |                     +------+------+
                                       |                            |
                                       | gRPC                       | gRPC
                                       |                            |
                                       +-------------+--------------+
                                                     |
                                                     v
                                              +-------------+
                                              |  Collector  |
                                              +------+------+
                                                     |
                                                     | Kafka
                                                     v
                                          stats.user-actions.v1
                                                     |
                                  +------------------+------------------+
                                  |                                     |
                                  v                                     v
                           +--------------+                       +------------+
                           |  Aggregator  |                       |  Analyzer  |
                           +------+-------+                       +------+-----+
                                  |                                      ^
                                  | Kafka                                |
                                  v                                      |
                      stats.events-similarity.v1 ------------------------+
                                                                         |
                                                                         | gRPC
                                                                         |
                                                                  event-service

     +---------------+
     |comment-service|
     +---------------+
            ^
            |
       Gateway / REST
~~~

### Бизнес-сервисы

`user-service` отвечает за пользователей.

`event-service` отвечает за события, категории, подборки, рейтинг событий, отправку действий `VIEW` и `LIKE`, а также получение персональных рекомендаций.

`request-service` отвечает за заявки на участие и отправляет действие `REGISTER` после создания заявки.

`comment-service` отвечает за комментарии и их модерацию.

### Инфраструктурные сервисы

`discovery-server` — Eureka Service Discovery.

`config-server` — централизованная конфигурация. Config Server запускается на случайном внутреннем порту и самостоятельно регистрируется в Eureka.

`gateway-server` — единая внешняя точка входа в REST API. Работает на порту `8080`.

Основные сервисы получают конфигурацию через Config Server Discovery, а не через фиксированный адрес Config Server.

### Подсистема рекомендаций

Подсистема расположена в `stats-service` и состоит из трёх основных сервисов.

`collector` принимает пользовательские действия по gRPC и публикует их в Kafka-топик:

~~~text
stats.user-actions.v1
~~~

`aggregator` читает действия пользователей, хранит максимальный вес действия пользователя для каждой пары пользователь/событие и инкрементально пересчитывает сходство событий.

Результаты публикуются в Kafka-топик:

~~~text
stats.events-similarity.v1
~~~

`analyzer` потребляет пользовательские действия и рассчитанные сходства, сохраняет данные в PostgreSQL и предоставляет gRPC API для:

- персональных рекомендаций;
- похожих событий;
- рейтинга событий.

### Legacy statistics API

Модули `stats-server` и `stats-dto` сохранены для обратной совместимости старого REST API статистики.

Доступ к нему снаружи также выполняется через Gateway:

~~~http
POST /hit
GET /stats
~~~

`event-service` больше не зависит от старого REST API статистики.

## Maven-модули

~~~text
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
+-- infra
|   |
|   +-- discovery-server
|   +-- config-server
|   +-- gateway-server
|
+-- stats-service
    |
    +-- stats-dto
    +-- stats-server
    +-- stats-client
    +-- stats-proto
    +-- stats-avro
    +-- collector
    +-- aggregator
    +-- analyzer
~~~

## Межсервисное взаимодействие

Для REST-взаимодействия бизнес-сервисов используются OpenFeign, Eureka и Spring Cloud LoadBalancer.

Общие Feign-контракты находятся в модуле:

~~~text
core/common
~~~

Основные внутренние клиенты:

~~~text
UserClient       -> user-service
EventClient      -> event-service
RequestClient    -> request-service
CommentClient    -> comment-service
~~~

Модуль `stats-client` содержит три клиента:

~~~text
StatsClient      -> legacy REST statistics API
CollectorClient  -> gRPC Collector
AnalyzerClient   -> gRPC Analyzer
~~~

Collector и Analyzer находятся через Eureka:

~~~properties
grpc.client.collector.address=discovery:///collector
grpc.client.analyzer.address=discovery:///analyzer
~~~

## Пользовательские действия

Рекомендательная система использует три типа действий:

| Действие | Вес |
|---|---:|
| `VIEW` | `0.4` |
| `REGISTER` | `0.8` |
| `LIKE` | `1.0` |

Для одной пары пользователь/событие учитывается только действие с максимальным весом.

Например, последовательность:

~~~text
VIEW -> REGISTER -> LIKE
~~~

даёт итоговый вес `1.0`, а не сумму `2.2`.

Повторное действие с весом не выше уже сохранённого не меняет статистику.

### VIEW

Действие отправляется при запросе конкретного события:

~~~http
GET /events/{id}
X-EWM-USER-ID: {userId}
~~~

Публичный список:

~~~http
GET /events
~~~

не создаёт `VIEW`.

### REGISTER

После создания заявки на участие через:

~~~http
POST /users/{userId}/requests?eventId={eventId}
~~~

`request-service` отправляет действие `REGISTER` в Collector.

### LIKE

~~~http
PUT /events/{eventId}/like
X-EWM-USER-ID: {userId}
~~~

LIKE разрешён только пользователю, который имеет подтверждённую заявку на мероприятие и дата начала мероприятия уже наступила (`eventDate` не находится в будущем).

## Рейтинг события

Поле `views` в DTO событий заменено на `rating`.

Analyzer рассчитывает рейтинг как сумму максимальных весов взаимодействий пользователей с событием.

Например:

~~~text
user 1 -> LIKE      = 1.0
user 2 -> REGISTER  = 0.8
user 3 -> VIEW      = 0.4

rating = 2.2
~~~

Рейтинг используется при формировании DTO событий.

## Сходство событий

Aggregator рассчитывает сходство событий на основании общих пользователей.

Для пользователя сначала выбирается максимальный вес взаимодействия с каждым событием.

Для пары событий используется нормализованная мера сходства:

~~~text
similarity(A, B) = S_min / sqrt(S_A * S_B)
~~~

где:

~~~text
S_A   — сумма весов взаимодействий с событием A
S_B   — сумма весов взаимодействий с событием B
S_min — сумма минимальных весов для пользователей,
        взаимодействовавших с обоими событиями
~~~

Пара событий хранится в каноническом порядке: событие с меньшим ID идёт первым.

Пересчёт выполняется инкрементально только при изменении максимального веса взаимодействия пользователя с событием.

## Персональные рекомендации

Analyzer формирует рекомендации на основании истории взаимодействий пользователя и сходства событий.

Алгоритм формирования рекомендаций:

1. выбираются последние `N` событий, с которыми взаимодействовал пользователь;
2. для этих событий находятся похожие мероприятия;
3. исключаются события, с которыми пользователь уже взаимодействовал;
4. из оставшихся кандидатов выбираются `N` наиболее похожих;
5. для каждого кандидата выбираются до `K` ближайших событий из истории пользователя;
6. прогнозируемая оценка кандидата рассчитывается на основании рейтингов взаимодействий пользователя и сходства событий;
7. кандидаты сортируются по рассчитанному `score` в порядке убывания;
8. возвращаются лучшие рекомендации.

Общая схема:

~~~text
история взаимодействий пользователя
                |
                v
       похожие мероприятия
                |
                v
 исключение уже известных событий
                |
                v
 выбор наиболее похожих кандидатов
                |
                v
 оценка по K ближайшим взаимодействиям
                |
                v
 сортировка по predicted score
                |
                v
 персональные рекомендации
~~~

REST endpoint:

~~~http
GET /events/recommendations
X-EWM-USER-ID: {userId}
~~~

`event-service` получает рекомендации от Analyzer по gRPC, загружает соответствующие опубликованные события из своей БД и возвращает `EventShortDto`.

## gRPC API

Protocol Buffers находятся в:

~~~text
stats-service/stats-proto/src/main/proto
~~~

Collector предоставляет:

~~~text
UserActionController
  CollectUserAction(UserActionProto)
~~~

Analyzer предоставляет:

~~~text
RecommendationsController
  GetRecommendationsForUser(...)
  GetSimilarEvents(...)
  GetInteractionsCount(...)
~~~

HTTP- и gRPC-порты Collector и Analyzer выбираются динамически.

gRPC-порт публикуется в metadata экземпляра сервиса в Eureka, а клиенты используют service discovery:

~~~properties
grpc.client.collector.address=discovery:///collector
grpc.client.analyzer.address=discovery:///analyzer
~~~

## Kafka

Используются два Kafka-топика:

~~~text
stats.user-actions.v1
stats.events-similarity.v1
~~~

`stats.user-actions.v1` содержит пользовательские действия.

`stats.events-similarity.v1` содержит обновления сходства мероприятий.

В Docker Compose Kafka доступна внутри сети как:

~~~text
kafka:9092
~~~

Для локальной диагностики с хоста:

~~~text
localhost:19094
~~~

## Хранение данных

Для бизнес-сервисов используется подход Database per Service.

| Сервис | База | Host port |
|---|---|---:|
| `stats-server` | `stats` | `6541` |
| `user-service` | `users` | `6543` |
| `request-service` | `requests` | `6544` |
| `event-service` | `events` | `6545` |
| `comment-service` | `comments` | `6546` |
| `analyzer` | `analyzer` | не публикуется |

Analyzer имеет отдельную PostgreSQL БД.

Между БД разных сервисов нет внешних ключей. Связи с объектами других сервисов хранятся в виде идентификаторов.

## Service Discovery

Eureka внутри Docker-сети работает на:

~~~text
discovery-server:8761
~~~

С хоста Eureka UI доступен по адресу:

~~~text
http://localhost:18761
~~~

В Eureka регистрируются, в частности:

~~~text
CONFIG-SERVER
GATEWAY-SERVER
USER-SERVICE
EVENT-SERVICE
REQUEST-SERVICE
COMMENT-SERVICE
STATS-SERVER
COLLECTOR
AGGREGATOR
ANALYZER
~~~

Большинство сервисов запускается на динамических внутренних портах.

`gateway-server` использует фиксированный внутренний порт `8080`. Основной внешний REST API доступен на host-порту `8080`; дополнительный mapping `9090 -> 8080` сохранён для совместимости с автоматическими CI-тестами.

## Config Server

Config Server также регистрируется в Eureka и использует случайный внутренний HTTP-порт.

Клиенты находят его через:

~~~properties
spring.cloud.config.discovery.enabled=true
spring.cloud.config.discovery.service-id=config-server
~~~

В `application.properties` клиентов используется:

~~~properties
spring.config.import=optional:configserver:
~~~

Это позволяет unit- и WebMvc-тестам запускаться без поднятой инфраструктуры.

При запуске через Docker Compose для Config Client сервисов устанавливается:

~~~text
SPRING_CLOUD_CONFIG_FAIL_FAST=true
~~~

Поэтому контейнер не продолжит запуск, если Config Server недоступен через Service Discovery.

Поэтому фиксированный внешний порт Config Server проекту не требуется.

Централизованные конфигурации находятся в:

~~~text
infra/config-server/src/main/resources/config/
~~~

В том числе:

~~~text
user-service.properties
event-service.properties
request-service.properties
comment-service.properties
stats-server.properties
gateway-server.properties
collector.properties
aggregator.properties
analyzer.properties
~~~

## API Gateway

Внешний REST API доступен через:

~~~text
http://localhost:8080
~~~

Основные маршруты:

~~~text
/admin/users/**                          -> user-service

/users/*/requests/**                    -> request-service
/users/*/events/*/requests/**           -> request-service

/events/*/comments/**                   -> comment-service
/users/*/comments/**                    -> comment-service
/users/*/events/*/comments/**           -> comment-service
/admin/comments/**                      -> comment-service

/events/**                              -> event-service
/categories/**                          -> event-service
/compilations/**                        -> event-service
/users/*/events/**                      -> event-service
/admin/events/**                        -> event-service
/admin/categories/**                    -> event-service
/admin/compilations/**                  -> event-service

/hit                                    -> stats-server
/stats                                  -> stats-server
~~~

## Основные REST endpoints

### События

~~~http
GET /events
GET /events/{id}
GET /events/recommendations
PUT /events/{eventId}/like
~~~

Для запросов конкретного события, рекомендаций и LIKE используется:

~~~http
X-EWM-USER-ID: {userId}
~~~

### Пользовательские события

~~~http
POST  /users/{userId}/events
GET   /users/{userId}/events
GET   /users/{userId}/events/{eventId}
PATCH /users/{userId}/events/{eventId}
~~~

### Заявки

~~~http
POST  /users/{userId}/requests?eventId={eventId}
GET   /users/{userId}/requests
PATCH /users/{userId}/requests/{requestId}/cancel
~~~

### Комментарии

~~~http
GET    /events/{eventId}/comments
GET    /events/{eventId}/comments/{commentId}

POST   /users/{userId}/events/{eventId}/comments
GET    /users/{userId}/comments
PATCH  /users/{userId}/comments/{commentId}
DELETE /users/{userId}/comments/{commentId}

GET    /admin/comments
PATCH  /admin/comments/{commentId}/publish
PATCH  /admin/comments/{commentId}/reject
DELETE /admin/comments/{commentId}
~~~

## Отказоустойчивость

Для HTTP-вызовов OpenFeign настроены таймауты.

Для получения некритичных агрегированных данных применяется graceful degradation.

В частности:

~~~text
недоступен comment-service
    -> event-service продолжает отвечать
    -> comments = 0

недоступен request-service
    -> выполняется Retry
    -> при окончательной ошибке confirmedRequests = 0
~~~

Retry применяется к операциям чтения.

Автоматические retry для изменяющих состояние запросов не используются, чтобы не допустить повторного выполнения операции.

## Запуск

Требования:

- Java 21;
- Maven;
- Docker;
- Docker Compose.

Запуск всей системы:

~~~bash
docker compose up -d --build
~~~

Проверка:

~~~bash
docker compose ps
~~~

После запуска:

~~~text
API Gateway : http://localhost:8080
Eureka      : http://localhost:18761
Kafka       : localhost:19094
~~~

Config Server и внутренние сервисы не требуют фиксированных host ports.

Остановка:

~~~bash
docker compose down
~~~

Удаление контейнеров вместе с persistent volumes:

~~~bash
docker compose down -v
~~~

## Сборка и тестирование

Полный Maven reactor:

~~~bash
mvn clean test
~~~

Сборка:

~~~bash
mvn clean package
~~~

Проверки качества:

~~~bash
mvn clean package -Pcheck
mvn clean verify -Pcoverage
~~~

## Проверенная интеграция

В проекте проверены:

- запуск системы через Docker Compose;
- регистрация сервисов в Eureka;
- Config Server Discovery;
- случайные внутренние порты сервисов;
- маршрутизация через Gateway;
- REST statistics API через Gateway;
- OpenFeign-взаимодействие бизнес-сервисов;
- gRPC-взаимодействие с Collector и Analyzer;
- отправка действий в Kafka;
- расчёт сходства событий;
- получение рейтингов;
- получение персональных рекомендаций;
- запрет LIKE для будущего события;
- Database per Service;
- graceful degradation зависимых сервисов.

## Спецификация API

Основная REST-спецификация:

[ewm-main-service-spec.json](./ewm-main-service-spec.json)

Legacy statistics API:

[ewm-stats-service-spec.json](./ewm-stats-service-spec.json)

Дополнительные Stage 3 endpoints рекомендаций и пользовательских действий описаны в этом README и реализованы в `event-service`.

## Ключевые свойства финальной архитектуры

Проект объединяет:

- Spring Cloud микросервисы;
- Service Discovery;
- централизованную конфигурацию;
- API Gateway;
- OpenFeign;
- gRPC;
- Kafka;
- Avro;
- PostgreSQL;
- Database per Service;
- персональные рекомендации;
- инкрементальный расчёт сходства событий;
- Docker Compose;
- автоматизированное тестирование;
- Checkstyle;
- SpotBugs;
- JaCoCo.