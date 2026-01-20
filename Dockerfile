# Stage 1: Build
# Use maven to build the project (after build, we will discard this image)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
# can use /, but /app is more safe, avoid conflict with system files like /tmp
WORKDIR /app

# Optimize Cache: Copy POM first and download dependencies
# This way, if pom.xml doesn't change, we can reuse the downloaded dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy Source Code and Build
# copy src folder in local, put into container's /app/src
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# use openjdk:21-jdk-slim as base image, it is smaller than the build image
FROM openjdk:21-jdk-slim
WORKDIR /app
# copy the jar file from build stage to runtime stage, as app.jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]



