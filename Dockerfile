# ===========================
# Build Stage
# ===========================
FROM maven:3.9.8-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests

# ===========================
# Runtime Stage
# ===========================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/expensetracker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]