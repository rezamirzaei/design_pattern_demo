# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Environment variables
ENV SPRING_PROFILES_ACTIVE=postgres
ENV DB_HOST=postgres
ENV DB_PORT=5432
ENV DB_NAME=smarthome
ENV DB_USERNAME=smarthome
ENV DB_PASSWORD=smarthome

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
