# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Install Maven
RUN apk add --no-cache maven ffmpeg ffmpeg-libs

# Set the working directory in the container
WORKDIR /app

# Copy the project files to the container
COPY . .

# Package the application using Maven
RUN mvn clean package -DskipTests

# Expose the port the application runs on
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/VinaAcademy-0.0.1-SNAPSHOT.jar"]