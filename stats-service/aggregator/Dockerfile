# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY . .

RUN --mount=type=cache,id=ewm-maven,target=/root/.m2,sharing=locked \
    mvn -Dmaven.wagon.http.retryHandler.count=5 \
    -pl stats-service/aggregator \
    -am \
    package \
    -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build \
    /app/stats-service/aggregator/target/aggregator-0.0.1-SNAPSHOT.jar \
    app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
