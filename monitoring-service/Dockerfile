# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:resolve

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/monitoring-service-*.jar app.jar

# Expose port
EXPOSE 8084

# Set environment variables with defaults
ENV PORT=8084 \
    DB_IP=postgres-monitoring \
    DB_PORT=5432 \
    DB_USER=vutzu \
    DB_PASSWORD=root \
    DB_DBNAME=monitoring_db \
    RABBITMQ_HOST=rabbitmq \
    RABBITMQ_PORT=5672 \
    RABBITMQ_USER=guest \
    RABBITMQ_PASSWORD=guest

# Run the application
CMD ["java", "-jar", "app.jar"]

