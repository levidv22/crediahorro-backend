FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/notificacion-service-0.0.1-SNAPSHOT.jar app.jar
ENV TWILIO_SID null
ENV TWILIO_TOKEN null
ENV EUREKA_URL null
ENTRYPOINT ["java","-jar","/app.jar"]