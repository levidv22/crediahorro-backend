FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
RUN mkdir -p /app/static/comprobantes && chmod -R 777 /app/static
COPY --from=build /app/target/admin-core-service-0.0.1-SNAPSHOT.jar /app/app.jar
ENV CONFIG_SERVER_HOST=null
ENV EUREKA_URL=null
ENV GATEWAY_URL=null
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
