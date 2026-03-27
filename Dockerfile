# Stage 1: Build stage (Use JDK 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw

COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run stage (Use JRE 21)
FROM eclipse-temurin:21-jre
WORKDIR /app
VOLUME /tmp

COPY --from=build /app/target/*.jar app.jar

COPY application.yml /app/application.yml

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.config.location=file:/app/application.yml", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "-Dserver.port=${PORT}", "-jar","app.jar"]