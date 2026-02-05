# === Backend (Spring Boot) Dockerfile ===
# Multi-stage build: use Gradle to build the JAR, then run it on a slim JRE image

# 1) BUILD STAGE
FROM gradle:8.7-jdk17-jammy AS backend-build

# Set workdir at the repo root to allow Gradle to resolve the multi-project build
WORKDIR /workspace

# Copy Gradle wrapper and settings first for better layer caching
COPY gradle gradle
COPY gradlew gradlew
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

# Copy the entire project (needed for :backend:bootJar)
COPY . .

# Build only the backend bootJar (skip tests for speed; remove -x test to run tests)
RUN chmod +x ./gradlew \
    && ./gradlew :backend:bootJar -x test --no-daemon

# 2) RUNTIME STAGE
FROM eclipse-temurin:17-jre-jammy AS backend-runtime

WORKDIR /app

# Copy the built Spring Boot jar
# This picks the only jar produced by the :backend:bootJar task
COPY --from=backend-build /workspace/backend/build/libs/*.jar /app/app.jar

# Configure JVM & Spring Boot defaults via environment variables
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
ENV SERVER_PORT=8080
# CORS: default to same-origin via nginx proxy; can be overridden in docker-compose
ENV APP_CORS_ALLOWED_ORIGIN=http://localhost:8080

EXPOSE 8080

# Note: Healthcheck can be defined in docker-compose; keeping image lean.

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
