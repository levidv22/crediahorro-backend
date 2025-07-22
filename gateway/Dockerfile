FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
COPY --from=build /app/target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENV EUREKA_URL null
ENV SPRING_PROFILES_ACTIVE=oauth2
ENTRYPOINT ["java","-jar","/app.jar"]