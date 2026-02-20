# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser
RUN chown -R appuser:appuser /app

COPY --from=build --chown=appuser:appuser /app/target/*.jar app.jar

USER appuser

# Environment variables
ENV SPRING_PROFILES_ACTIVE=postgres
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=smarthome
ENV DB_USERNAME=smarthome
ENV DB_PASSWORD=smarthome

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/status || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
