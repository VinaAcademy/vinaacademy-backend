# Stage 1: Build stage
FROM openjdk:17-jdk-alpine AS builder

# Thiết lập thư mục làm việc
WORKDIR /app

# Copy Maven wrapper và pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Cấp quyền thực thi cho Maven wrapper
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

LABEL maintainer="VinaAcademy"
LABEL description="VinaAcademy Platform"

# Install Maven
RUN apk add --no-cache ffmpeg ffmpeg-libs curl \
    && addgroup -g 1001 -S vinaacademy \
    && adduser -u 1001 -S vinaacademy -G vinaacademy

# Set the working directory in the container
WORKDIR /app
COPY --from=builder /app/target/VinaAcademy-*.jar app.jar
RUN chown vinaacademy:vinaacademy app.jar

USER vinaacademy

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]