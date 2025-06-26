
# Stage 1: Build stage using Maven and Java 17
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /workspace

# Copy all files from the current directory on the host to the container
COPY . .

# Run Maven build: compile and package only the energy-rest-api module,
# including dependencies (-am), but skip tests to speed up the build.
RUN mvn clean package -pl energy-rest-api -am -DskipTests

# Stage 2: Runtime stage using a minimal Java 17 image
FROM openjdk:17-jdk-slim

# Set working directory for the runtime container
WORKDIR /app

# Copy the built JAR file from the previous stage into this image
COPY --from=builder /workspace/energy-rest-api/target/energy-rest-api-0.0.1-SNAPSHOT.jar app.jar

# Define the default command to run the application
CMD ["java", "-jar", "app.jar"]