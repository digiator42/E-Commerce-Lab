# Stage 1: Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper and pom first (efficient caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw

# Copy source and build
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
VOLUME /tmp

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render dynamic port binding
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]