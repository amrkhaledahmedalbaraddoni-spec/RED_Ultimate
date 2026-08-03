# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
# Download dependencies first for better layer caching
RUN gradle --no-daemon dependencies --quiet || true
COPY src ./src
RUN gradle --no-daemon bootJar -x test

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/backend-1.0.0.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
