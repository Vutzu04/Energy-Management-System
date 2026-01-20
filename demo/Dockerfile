# Use OpenJDK 25 as base image
FROM openjdk:25-jdk-slim

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
