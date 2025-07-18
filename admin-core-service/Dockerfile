FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/admin-core-service-0.0.1-SNAPSHOT.jar app.jar
ENV CONFIG_SERVER_HOST null
ENV EUREKA_URL null
ENTRYPOINT ["java","-jar","/app.jar"]