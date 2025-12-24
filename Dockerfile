# Stage 1: Build (We use Maven to create the .jar)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# We compile skipping tests to speed up deployment
RUN mvn clean package -DskipTests

# Stage 2: Runtime (We use a lightweight Java image to run the app)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copy the .jar generated in stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Startup command
ENTRYPOINT ["java", "-jar", "app.jar"]
