FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .

RUN --mount=type=cache,id=ewm-maven,target=/root/.m2,sharing=locked \
    mvn -Dmaven.wagon.http.retryHandler.count=5 -pl core/event-service -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/core/event-service/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]