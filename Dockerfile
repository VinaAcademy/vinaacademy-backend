# Build stage
FROM maven:3.8.3-openjdk-17-slim AS build

WORKDIR /app

COPY ./pom.xml .

RUN mvn dependency:go-offline -B

COPY ./src ./src
RUN mvn clean package -DskipTests

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
COPY --from=build /app/target/VinaAcademy-*.jar app.jar
RUN chown vinaacademy:vinaacademy app.jar

USER vinaacademy

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]