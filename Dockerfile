FROM openjdk:21-jdk-slim

LABEL maintainer="OrganSync Team"
LABEL description="OrganSync Matching Service - Advanced kidney exchange matching"

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw package -DskipTests

# Create runtime image
FROM openjdk:21-jre-slim

WORKDIR /app

# Copy built jar
COPY --from=0 /app/target/*.jar matching-service.jar

# Expose port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/matching/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "matching-service.jar"]