FROM eclipse-temurin:24-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ARG SENDGRID_API_KEY
ENV AWS_S3_BUCKET=""
ENV AWS_REGION=""
ENV AWS_ACCESS_KEY_ID=""
ENV AWS_SECRET_ACCESS_KEY=""
ENV SENDGRID_API_KEY=${SENDGRID_API_KEY}

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]