FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/report-ms-0.0.1-SNAPSHOT.jar app.jar
ENV CONFIG_SERVER_HOST null
ENV EUREKA_URL null
ENTRYPOINT ["java","-jar","/app.jar"]