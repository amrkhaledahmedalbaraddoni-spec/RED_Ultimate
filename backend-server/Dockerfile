# ---- Build stage ----
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
# Download dependencies first for better layer caching
RUN gradle --no-daemon dependencies --quiet
COPY src ./src
RUN gradle --no-daemon test bootJar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Add a non-root user for security
RUN groupadd -r red && useradd -r -g red red

COPY --from=build /app/build/libs/backend-1.0.0.jar /app/app.jar

# Create data directory
RUN mkdir -p /app/data && chown -R red:red /app

USER red

EXPOSE 8080

# JVM tuning for containerized environments
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
