# VinaAcademy Backend - AI Coding Guidelines

## Project Overview
**VinaAcademy Backend** is a Spring Boot 3.4.9 microservice for an online learning platform with feature-based modular architecture, gRPC communication, and comprehensive course management capabilities.

## Core Architecture Patterns

### Feature-Based Module Structure
```
feature/{domain}/
├── {Domain}Controller.java               # REST API endpoints
├── {Domain}Repository.java               # Data access layer  
├── {Domain}Service.java & ServiceImpl    # Business logic
├── entity/{Domain}.java                  # JPA entities
├── dto/{Domain}Dto.java, Request.java    # Data transfer objects
├── mapper/{Domain}Mapper.java            # MapStruct mappers
└── constant/, enums/, exceptions/        # Supporting classes
```

### Key Features
- **course/** - Course management with sections/lessons
- **user/** - Authentication & authorization with JWT/OAuth2
- **quiz/** - Interactive quiz system with automated grading
- **lesson/** - Video/Reading/Quiz content with progress tracking
- **enrollment/** - Student course enrollment & progress
- **order_payment/** - Payment processing with VNPay integration
- **storage/** - File upload to MinIO/S3 with presigned URLs
- **email/** - Thymeleaf template-based email system
- **grpc/** - gRPC services for microservice communication
- **discussion/** - Course discussion functionality
- **notification/** - Real-time notification system

## Technology Stack

### Core Framework
- **Spring Boot 3.4.9** with Java 17
- **Spring Data JPA** with PostgreSQL
- **Spring Security** with OAuth2 & JWT
- **Spring gRPC** for inter-service communication

### Core Framework
- **Spring Boot 3.4.9** with Java 17
- **Spring Data JPA** with PostgreSQL
- **Spring Security** with OAuth2 & JWT
- **Spring gRPC** for inter-service communication

### Data & Caching
- **PostgreSQL** - Primary database with JSON columns
- **Redis** - Caching & session storage
- **MinIO/S3** - File storage with presigned URLs

### Processing & Integration
- **MapStruct 1.4.1** - DTO/Entity mapping with `INSTANCE` pattern
- **Thymeleaf** - Email template engine
- **FFmpeg** - Video processing (Docker-based)
- **Protocol Buffers** - gRPC service definitions

## Development Environment

### Quick Start
```bash
# Start infrastructure services
docker-compose -f docker-compose.dev.yml up -d

# Run application (auto-creates test data)
./mvnw spring-boot:run

# Available endpoints:
# HTTP: http://localhost:8080
# gRPC: localhost:9090
# Swagger: http://localhost:8080/swagger-ui.html
```

### Services
- **PostgreSQL**: localhost:5432 (user: postgres, pass: postgres)
- **Redis**: localhost:6379
- **MinIO**: localhost:9000 (admin/password123, UI: 9001)

## Coding Conventions

### Security & Authorization
```java
// Get current authenticated user
User user = SecurityHelper.getCurrentUser();

// Resource-level authorization (AOP-based)
@RequiresResourcePermission(
    resourceType = ResourceConstants.COURSE,
    permission = ResourceConstants.MODIFY,
    idParam = "courseId"
)
public void updateCourse(UUID courseId, CourseRequest request) {
    // Implementation
}
```

### Entity Patterns
```java
// All entities extend BaseEntity for audit fields
@Entity
public class Course extends BaseEntity {
    @Id
    private UUID id;
    // Audit fields inherited: createdDate, updatedDate, createdBy, lastModifiedBy
}

// Lesson inheritance with JPA JOINED strategy
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "lesson_type")
public abstract class Lesson extends BaseEntity {
    // Common lesson fields
}
```

### MapStruct Configuration
```java
@Mapper
public interface CourseMapper {
    CourseMapper INSTANCE = Mappers.getMapper(CourseMapper.class);
    
    CourseDto toDto(Course course);
    Course toEntity(CourseRequest request);
}
```

### Transaction Boundaries
- Use `@Transactional(readOnly = true)` for queries
- Use `@Transactional` for write operations
- Service layer handles transaction boundaries

### API Standards
- RESTful endpoints with `/api/v1/` prefix
- Consistent `ApiResponse<T>` wrapper for responses
- Swagger documentation with `@Operation`, `@ApiResponse`
- Pagination support with `PaginationResponse<T>`

## gRPC Integration

### Service Definition (proto files in `src/main/proto/`)
```protobuf
service JwtService {
  rpc validateToken(TokenRequest) returns (ValidateTokenResponse);
}
```

### Implementation Pattern
```java
@GrpcService
@RequiredArgsConstructor
public class JwtServiceGrpcImpl extends JwtServiceGrpc.JwtServiceImplBase {
    
    @PreAuthorize("hasAuthority('SCOPE_api.read')")
    @Override
    public void validateToken(TokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        // Implementation
    }
}
```

## File Storage (MinIO/S3)

### Configuration
- Development: MinIO on localhost:9000
- Presigned URLs for secure file access
- S3Presigner bean configured in `S3Config`

### Usage Pattern
```java
// Generate presigned upload URL
String uploadUrl = s3Service.generatePresignedUploadUrl(bucketName, fileName);

// Upload file and get permanent URL
String fileUrl = s3Service.uploadFile(bucketName, fileName, inputStream);
```

## Common Development Patterns

### Lesson Creation (Factory Pattern)
```java
// Use LessonCreatorFactory for type-specific lesson creation
LessonCreator creator = lessonCreatorFactory.getCreator(lessonType);
Lesson lesson = creator.createLesson(lessonRequest, section, author);
```

### Email Templates
- Templates in `src/main/resources/templates/email/`
- Fragment-based layout: `layout.html`, `header.html`, `footer.html`
- Context variables passed via Thymeleaf `Context`

### Testing Data
- `TestingDataService` automatically creates seed data on startup
- Test users, courses, and sample content from JSON files
- Configurable via application properties

## Critical Gotchas
- Always use `SecurityHelper.getCurrentUser()` instead of direct SecurityContext access
- Lesson entities require section assignment and author before persistence
- Course-instructor relationships use `CourseInstructor` join entity
- MapStruct requires exact `INSTANCE` pattern for consistency
- File uploads require proper content-type detection via Apache Tika
- gRPC methods need `@PreAuthorize` for OAuth2 scope validation
