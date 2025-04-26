# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/volleyball-finder-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=90.0", "-jar", "app.jar"]