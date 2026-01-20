FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "target/device-management-service-0.0.1-SNAPSHOT.jar"]

