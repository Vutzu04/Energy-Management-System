FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Final stage
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/customer-support-service-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8086

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

