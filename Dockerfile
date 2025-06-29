
FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app

# Install git for spring-devtools to work
RUN apk add --no-cache git

# Only copy the files needed for Maven to download dependencies
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# For development, we'll run through mvn spring-boot:run
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]