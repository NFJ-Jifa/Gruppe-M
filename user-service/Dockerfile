FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
COPY . .
RUN mvn clean package -pl user-service -am -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /workspace/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar
CMD ["java","-jar","app.jar"]
