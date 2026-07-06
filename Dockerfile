# Stage 1: Build the application using Maven
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the build configuration and source code
COPY pom.xml .
COPY src ./src

# Build the application inside the container
RUN mvn clean package -DskipTests

# Stage 2: Create the final running image
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar directly from the first stage
COPY --from=build /app/target/BankingMgt-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]