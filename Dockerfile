FROM ubuntu:latest
LABEL authors="TAPPR-BACKEND"

ENTRYPOINT ["top", "-b"]

FROM maven:3.8.7 as build
COPY . .
RUN mvn -B clean package -DskipTests

FROM openjdk:17
COPY --from=build ./target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "-Dserver.port=8081", "app.jar"]
