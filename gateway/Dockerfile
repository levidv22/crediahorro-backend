FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENV EUREKA_URL null
ENV SPRING_PROFILES_ACTIVE=oauth2
ENTRYPOINT ["java","-jar","/app.jar"]