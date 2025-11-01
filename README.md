# VinaAcademy Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](#)

> A **microservices-based backend platform** for an online learning ecosystem featuring course management, interactive quizzes, payment processing, and real-time notifications.

## 📋 Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Usage & Examples](#usage--examples)
- [API Documentation](#api-documentation)
- [Development](#development)
- [Testing](#testing)
- [Contributing](#contributing)
- [Team](#team)

## 🎯 Overview

VinaAcademy Backend is a robust, feature-rich Spring Boot 3.4.9 microservice application designed for an online learning platform. Built with a **feature-based modular architecture**, it provides comprehensive course management, student enrollment, interactive quizzes with automated grading, payment processing, and real-time notifications.

The platform leverages modern technologies including **gRPC** for inter-service communication, **PostgreSQL** for data persistence, **Redis** for caching, and **MinIO/S3** for secure file storage.

## ✨ Key Features

### Core Capabilities

- **📚 Course Management** - Create, update, and manage courses with sections and lessons
- **👥 User Management** - Authentication & authorization with JWT and OAuth2 support
- **❓ Quiz System** - Interactive quizzes with automated grading and instant feedback
- **📖 Lesson Management** - Support for video, reading, and quiz content with progress tracking
- **🎓 Enrollment System** - Student enrollment management with progress tracking
- **💳 Payment Processing** - Secure payment handling with VNPay integration
- **📁 File Storage** - Secure file uploads to MinIO/S3 with presigned URL support
- **✉️ Email Notifications** - Thymeleaf template-based email communication
- **🗣️ Discussion Forum** - Course discussion functionality for student engagement
- **🔔 Real-Time Notifications** - Instant notifications using Kafka and WebSocket
- **🔐 Resource-Level Authorization** - AOP-based permission management
- **📊 Data Analytics** - Comprehensive course analytics and reporting

### Advanced Features

- **gRPC Communication** - High-performance inter-microservice communication
- **Session Management** - Redis-backed distributed session storage
- **Audit Trails** - Automatic tracking of entity creation and modifications
- **JWT Token Validation** - Secure token validation through gRPC services
- **Video Processing** - FFmpeg integration for video content optimization (Docker-based)

## 🛠️ Technology Stack

### Framework & Core
- **Spring Boot 3.4.9** - Enterprise Java application framework
- **Java 17** - Programming language and runtime
- **Spring Data JPA** - ORM and data access layer
- **Spring Security** - Authentication and authorization
- **OAuth2 & JWT** - Token-based authentication

### Data & Storage
- **PostgreSQL** - Relational database with JSON support
- **Redis** - Distributed caching and session storage
- **MinIO / AWS S3** - Object storage for file uploads
- **Hibernate** - JPA implementation

### Inter-Service Communication
- **gRPC 1.72.0** - High-performance RPC framework
- **Protocol Buffers** - Service definition and serialization
- **Spring Cloud 2024.0.2** - Microservice orchestration

### Data Mapping & Processing
- **MapStruct 1.4.1** - DTO/Entity object mapping
- **Apache Tika** - Content type detection
- **Thymeleaf** - Server-side template engine for emails

### Event Processing & Integration
- **Kafka** - Distributed event streaming platform
- **AWS SDK 2.32.32** - Cloud services integration

### Development Tools
- **Maven** - Build automation and dependency management
- **Lombok** - Boilerplate code reduction
- **Swagger/Springdoc OpenAPI** - API documentation

## 📋 Prerequisites

Before getting started, ensure you have the following installed:

### Required
- **Java Development Kit (JDK)** - Version 17 or higher
- **Apache Maven** - Version 3.8.1 or higher
- **Docker & Docker Compose** - For containerized infrastructure services
- **Git** - Version control system

### Services (Can be run via Docker)
- **PostgreSQL 13+** - Relational database
- **Redis 7+** - In-memory cache
- **MinIO** - S3-compatible object storage
- **Kafka 3+** - Event streaming (optional for basic setup)

### Verification Commands
```bash
java -version          # Should show Java 17+
mvn -version          # Should show Maven 3.8.1+
docker --version      # Docker command-line
docker-compose --version  # Docker Compose
```

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/VinaAcademy/vinaacademy-backend.git
cd vinaacademy-backend
```

### 2. Configure Environment Variables

Create an `.env` file in the project root (or set in your IDE):

```env
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vinaacademy
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis Configuration
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# File Storage (MinIO)
MINIO_ENDPOINT=http://localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# JWT/Security
JWT_SECRET=your_secret_key_here
JWT_EXPIRATION=86400000

# Email Configuration
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your_email@gmail.com
SPRING_MAIL_PASSWORD=your_app_password
```

### 3. Start Infrastructure Services

Start all required services using Docker Compose:

```bash
docker-compose -f docker-compose.dev.yml up -d
```

This will launch:
- **PostgreSQL** on `localhost:5432`
- **Redis** on `localhost:6379`
- **MinIO** on `localhost:9000` (UI: `localhost:9001`)
- **Kafka** on `localhost:9092`

### 4. Build the Project

```bash
# Using Maven wrapper (no need to install Maven separately)
./mvnw clean install

# Or if using installed Maven
mvn clean install
```

### 5. Run the Application

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or directly run the jar
java -jar target/VinaAcademy-0.0.1-SNAPSHOT.jar
```

The application will start on:
- **HTTP**: http://localhost:8080
- **gRPC**: localhost:9090
- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Verification

Check if the application is running:

```bash
# Check HTTP endpoint
curl http://localhost:8080/actuator/health

# Expected response: {"status":"UP"}
```

## 📁 Project Structure

```
vinaacademy-backend/
├── src/
│   ├── main/
│   │   ├── java/com/vinaacademy/platform/
│   │   │   ├── VinaAcademyApplication.java        # Main entry point
│   │   │   ├── configuration/                      # Spring configurations
│   │   │   │   ├── cache/                         # Redis configuration
│   │   │   │   ├── security/                      # Spring Security setup
│   │   │   │   └── ...
│   │   │   ├── exception/                          # Global exception handling
│   │   │   ├── feature/                            # Feature modules
│   │   │   │   ├── course/                        # Course management
│   │   │   │   ├── user/                          # User authentication
│   │   │   │   ├── quiz/                          # Quiz system
│   │   │   │   ├── lesson/                        # Lesson content
│   │   │   │   ├── enrollment/                    # Enrollment management
│   │   │   │   ├── order_payment/                 # Payment processing
│   │   │   │   ├── storage/                       # File storage
│   │   │   │   ├── email/                         # Email notifications
│   │   │   │   ├── discussion/                    # Discussion forum
│   │   │   │   ├── notification/                  # Real-time notifications
│   │   │   │   └── ...
│   │   │   ├── grpc/                               # gRPC service implementations
│   │   │   └── kafka/                              # Kafka event handlers
│   │   ├── proto/                                  # Protocol Buffer definitions
│   │   │   ├── jwt_service.proto
│   │   │   └── user_service.proto
│   │   └── resources/
│   │       ├── application.yml                     # Main config
│   │       ├── application-dev.yml                 # Development config
│   │       ├── application-prd.yml                 # Production config
│   │       ├── templates/                          # Email templates
│   │       └── data/                               # Seed data
│   └── test/                                       # Unit and integration tests
├── docs/                                           # Documentation
├── docker-compose.dev.yml                          # Docker Compose configuration
├── Dockerfile                                      # Application Docker image
├── pom.xml                                         # Maven configuration
└── README.md                                       # This file

```

### Feature Module Structure

Each feature follows a consistent pattern:

```
feature/{domain}/
├── {Domain}Controller.java               # REST API endpoints
├── {Domain}Repository.java               # Database access
├── {Domain}Service.java                  # Interface
├── service/{Domain}ServiceImpl.java       # Implementation
├── entity/{Domain}.java                  # JPA entity
├── dto/
│   ├── {Domain}Dto.java                 # Response DTO
│   ├── {Domain}Request.java             # Request DTO
│   └── ...
├── mapper/{Domain}Mapper.java            # MapStruct mapper
├── constant/                             # Feature constants
├── enums/                                # Feature enums
└── exceptions/                           # Feature-specific exceptions
```

## ⚙️ Configuration

### Application Configuration Files

#### `application.yml` (Main)
Default configuration applied to all profiles.

#### `application-dev.yml` (Development)
Development-specific settings with verbose logging and test data creation.

```yaml
spring:
  profiles:
    active: dev, mail
  jpa:
    hibernate:
      ddl-auto: update
  sql:
    init:
      mode: always
```

#### `application-prd.yml` (Production)
Production settings with optimized performance and security.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

#### `application-mail.yml` (Email)
Email service configuration for Thymeleaf-based notifications.

### Key Properties

```yaml
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/vinaacademy
spring.datasource.username=postgres
spring.datasource.password=postgres

# gRPC
grpc.server.port=9090
grpc.server.enable-keep-alive=true

# JWT Security
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000  # 24 hours

# Redis Caching
spring.redis.host=localhost
spring.redis.port=6379

# MinIO/S3 Storage
minio.endpoint=${MINIO_ENDPOINT}
minio.bucket.name=vinaacademy
```

## 💻 Usage & Examples

### 1. Create a New Course

```bash
curl -X POST http://localhost:8080/api/v1/courses \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Spring Boot Masterclass",
    "description": "Learn Spring Boot from basics to advanced",
    "category": "Programming",
    "level": "INTERMEDIATE",
    "price": 29.99
  }'
```

### 2. Enroll a Student in a Course

```bash
curl -X POST http://localhost:8080/api/v1/enrollments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "550e8400-e29b-41d4-a716-446655440000",
    "studentId": "550e8400-e29b-41d4-a716-446655440001"
  }'
```

### 3. Create and Submit a Quiz

```bash
curl -X POST http://localhost:8080/api/v1/quizzes \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring Boot Basics Quiz",
    "lessonId": "550e8400-e29b-41d4-a716-446655440002",
    "questions": [
      {
        "question": "What is Spring Boot?",
        "options": ["A framework", "A library", "A language"],
        "correctAnswer": 0,
        "points": 10
      }
    ],
    "totalPoints": 100,
    "passingScore": 70
  }'
```

### 4. Upload a Course Video

```bash
# Get presigned upload URL
curl -X GET http://localhost:8080/api/v1/storage/upload-url?fileName=course-intro.mp4 \
  -H "Authorization: Bearer YOUR_TOKEN"

# Upload file to the presigned URL
curl -X PUT "<PRESIGNED_URL>" \
  -H "Content-Type: video/mp4" \
  --data-binary @course-intro.mp4
```

### 5. Process Payment with VNPay

```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-2024-001",
    "amount": 299.99,
    "currency": "VND",
    "paymentMethod": "VNPAY",
    "description": "Course enrollment payment"
  }'
```

## 📚 API Documentation

### Interactive Documentation
The API is fully documented with Swagger. Access it at:

```
http://localhost:8080/swagger-ui.html
```

### Key API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/courses` | GET | List all courses |
| `/api/v1/courses` | POST | Create a new course |
| `/api/v1/courses/{id}` | GET | Get course details |
| `/api/v1/courses/{id}` | PUT | Update course |
| `/api/v1/courses/{id}` | DELETE | Delete course |
| `/api/v1/enrollments` | POST | Enroll student |
| `/api/v1/quizzes` | POST | Create quiz |
| `/api/v1/quizzes/{id}/submit` | POST | Submit quiz answers |
| `/api/v1/lessons` | POST | Create lesson |
| `/api/v1/discussions` | GET | Get discussions |
| `/api/v1/payments` | POST | Process payment |

## 🛠️ Development

### Code Style & Conventions

#### Security Helper
```java
// Always use SecurityHelper to get current authenticated user
User user = SecurityHelper.getCurrentUser();
String userId = user.getId();
```

#### Entity Patterns
```java
// All entities extend BaseEntity for audit fields
@Entity
public class Course extends BaseEntity {
    @Id
    private UUID id;
    private String title;
    // Inherited fields: createdDate, updatedDate, createdBy, lastModifiedBy
}
```

#### MapStruct Configuration
```java
@Mapper
public interface CourseMapper {
    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);
    
    CourseDto toDto(Course course);
    Course toEntity(CourseRequest request);
}
```

#### Service Layer Transactions
```java
@Service
public class CourseServiceImpl implements CourseService {
    
    @Transactional(readOnly = true)
    public CourseDto getCourse(UUID courseId) {
        // Query logic
    }
    
    @Transactional
    public CourseDto createCourse(CourseRequest request) {
        // Write logic
    }
}
```

#### gRPC Services
```java
@GrpcService
@RequiredArgsConstructor
public class JwtServiceGrpcImpl extends JwtServiceGrpc.JwtServiceImplBase {
    
    @PreAuthorize("hasAuthority('SCOPE_api.read')")
    @Override
    public void validateToken(TokenRequest request, 
                            StreamObserver<ValidateTokenResponse> responseObserver) {
        // Implementation
    }
}
```

### Building & Compilation

```bash
# Full build with tests
./mvnw clean install

# Build without tests
./mvnw clean install -DskipTests

# Compile only
./mvnw clean compile

# Generate gRPC code from .proto files
./mvnw protobuf:compile protobuf:compile-custom
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=CourseServiceTest

# Run with coverage report
./mvnw jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Structure

Tests are located in `src/test/java/` and follow the same package structure as source code.

Example test:
```java
@SpringBootTest
class CourseServiceTest {
    
    @Autowired
    private CourseService courseService;
    
    @Test
    void testCreateCourse() {
        // Test implementation
    }
}
```

## 📦 Building for Production

### Create Production Build

```bash
# Build JAR with production profile
./mvnw clean install -P prd -DskipTests

# JAR will be created at: target/VinaAcademy-0.0.1-SNAPSHOT.jar
```

### Docker Build

```bash
# Build Docker image
docker build -t vinaacademy-backend:latest .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/vinaacademy \
  -e SPRING_REDIS_HOST=redis \
  vinaacademy-backend:latest
```

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### Getting Started
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes and commit: `git commit -m 'Add your feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Submit a pull request

### Development Guidelines
- Follow existing code style and conventions
- Write clear commit messages
- Add unit tests for new features
- Update documentation as needed
- Ensure all tests pass before submitting PR

### Reporting Issues
- Use clear, descriptive titles
- Include steps to reproduce the issue
- Provide relevant code snippets or screenshots
- Specify your environment (Java version, OS, etc.)

## 👥 Team

The VinaAcademy Backend project is developed and maintained by:

- **Nguyen Huu Loc**
- **Phan Thi My Linh**
- **Vuong Tri Hung**

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support & Resources

- **Documentation**: See `/docs` folder for detailed guides
- **API Documentation**: http://localhost:8080/swagger-ui.html
- **Issues**: [GitHub Issues](https://github.com/VinaAcademy/vinaacademy-backend/issues)
- **Pull Requests**: [GitHub Pull Requests](https://github.com/VinaAcademy/vinaacademy-backend/pulls)

---

**Happy Coding! 🚀**
