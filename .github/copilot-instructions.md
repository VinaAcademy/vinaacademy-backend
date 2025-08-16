# VinaAcademy Backend - AI Coding Guidelines

# Project Overview

## Folder Structure

```
vinaacademy-backend/
├── .github/
│   └── copilot-instructions.md           # AI coding guidelines
├── docker-compose.dev.yml               # Development environment setup
├── Dockerfile                           # Container configuration
├── docs/                               # Documentation
│   ├── auth-workflow.md
│   └── refactoring/
├── pom.xml                             # Maven configuration
└── src/
    ├── main/
    │   ├── java/com/vinaacademy/platform/
    │   │   ├── VinaAcademyApplication.java  # Main application entry point
    │   │   ├── configuration/               # App-wide configuration
    │   │   │   ├── security/               # Security configurations
    │   │   │   ├── cache/                 # Redis cache setup
    │   │   │   ├── JacksonConfig.java     # JSON serialization config
    │   │   │   ├── SwaggerConfig.java     # API documentation config
    │   │   │   └── AuditingConfig.java    # JPA auditing setup
    │   │   ├── exception/                  # Global exception handling
    │   │   └── feature/                   # Feature-based modules
    │   │       ├── cart/                  # Shopping cart functionality
    │   │       ├── category/              # Course categories
    │   │       ├── common/                # Shared utilities & base classes
    │   │       ├── course/                # Course management
    │   │       ├── email/                 # Email service with templates
    │   │       ├── enrollment/            # Student course enrollment
    │   │       ├── instructor/            # Instructor management
    │   │       ├── lesson/                # Lesson content (Video/Quiz/Reading)
    │   │       ├── notification/          # Notification system
    │   │       ├── order_payment/         # Payment processing & coupons
    │   │       ├── quiz/                  # Quiz system with grading
    │   │       ├── review/                # Course reviews & ratings
    │   │       ├── section/               # Course sections
    │   │       ├── storage/               # File upload & management
    │   │       ├── user/                  # User management & authentication
    │   │       └── video/                 # Video processing & streaming
    │   └── resources/
    │       ├── application*.yml           # Configuration files
    │       ├── data/                     # Seed data (JSON)
    │       ├── static/                   # Static assets
    │       └── templates/email/          # Thymeleaf email templates
    └── test/                            # Unit & integration tests
```

## Libraries and Frameworks

### Core Framework
- **Spring Boot 3.4.3** - Main application framework with Java 17
- **Spring Data JPA** - Database abstraction with Hibernate ORM
- **Spring Security** - Authentication & authorization
- **Spring Web** - REST API development
- **Spring Validation** - Bean validation with Jakarta annotations

### Security & Authentication
- **OAuth2 Resource Server** - JWT token-based authentication
- **OAuth2 Client** - External OAuth integration support
- **Spring Security** - Role-based access control

### Database & Caching
- **PostgreSQL** - Primary relational database
- **Redis** - Caching, session storage, and message queuing
- **Hibernate** - ORM with audit trails and soft deletes

### Data Processing & Mapping
- **MapStruct 1.4.1** - Type-safe bean mapping between DTOs and entities
- **Jackson** - JSON serialization/deserialization
- **Apache Commons Lang3** - Utility functions

### Email & Templates
- **Spring Mail** - Email sending capabilities
- **Thymeleaf** - Server-side template engine for HTML emails
- **Thymeleaf Spring Security** - Security integration for templates

### File Processing & Validation
- **Apache Tika 3.1.0** - File type detection and content analysis
- **Commons Codec** - Encoding/decoding utilities
- **Commons BeanUtils** - Bean manipulation utilities

### API Documentation & Monitoring
- **SpringDoc OpenAPI 2.8.5** - Swagger/OpenAPI documentation generation
- **Spring Actuator** - Application monitoring and health checks

### Build & Development Tools
- **Maven** - Build automation and dependency management
- **Lombok** - Code generation for boilerplate reduction
- **Spring Boot DevTools** - Development-time tools
- **Spring Retry** - Retry mechanism for transient failures

### Background Processing
- **Spring Batch** - Batch job processing
- **Spring AOP** - Aspect-oriented programming for cross-cutting concerns
- **Spring WebSocket** - Real-time communication support

## Coding Standards

### Code Organization
- **Feature-Based Architecture**: Each domain (course, user, quiz) has its own package with controllers, services, repositories, DTOs, and mappers
- **Layered Architecture**: Controller → Service → Repository pattern with clear separation of concerns
- **Package by Feature**: `feature.{domain}.{layer}` structure for better modularity

### Naming Conventions
- **Classes**: PascalCase (e.g., `CourseService`, `UserRepository`)
- **Methods**: camelCase (e.g., `getCourseById`, `createNewUser`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_FILE_SIZE`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase with dots (e.g., `com.vinaacademy.platform.feature.course`)

### Annotation Standards
- **Validation**: Use Jakarta validation annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`)
- **Security**: Use `@HasAnyRole` for role-based access control
- **Transactions**: Use `@Transactional(readOnly = true)` for queries, `@Transactional` for writes
- **Logging**: Use `@Slf4j` from Lombok for consistent logging
- **API Documentation**: Use Swagger annotations (`@Operation`, `@ApiResponses`, `@Schema`)

### MapStruct Configuration
All mappers must use consistent configuration:
```java
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
```

### Exception Handling
- Custom exceptions extend base exceptions (`BadRequestException`, `NotFoundException`, `ValidationException`)
- Global exception handler in `GlobalExceptionHandler` for consistent error responses
- Use `@ControllerAdvice` for centralized exception handling

### Security Patterns
- Use `SecurityHelper.getCurrentUser()` to get authenticated user context
- Authorization checks via `AuthorizationService` with AOP aspects
- Resource-level permissions with `@RequiresResourcePermission`
- JWT validation through OAuth2 resource server configuration

### Database Standards
- All entities extend `BaseEntity` for audit fields (createdDate, updatedDate, createdBy, lastModifiedBy)
- Use UUIDs for primary keys for better distributed system support
- Soft deletes with `SoftDeleteEntity` for data retention
- JSON columns use PostgreSQL `jsonb` type for efficient querying

### API Standards
- RESTful endpoints with proper HTTP methods (GET, POST, PUT, DELETE)
- Consistent response format using `ApiResponse<T>` wrapper
- Pagination support with `PaginationResponse<T>`
- OpenAPI documentation for all public endpoints
- Version prefix `/api/v1/` for all endpoints

### Code Quality
- Use `@UtilityClass` for utility classes with static methods
- Prefer composition over inheritance
- Use builder pattern for complex object creation
- Validate inputs at controller and service layers
- Use caching annotations for frequently accessed data

## Architecture Overview

This is a Spring Boot microservice for an online learning platform with feature-based modular architecture. Each feature (course, user, quiz, etc.) contains its own controllers, services, repositories, DTOs, and mappers within dedicated packages.

### Core Domain Model
- **Course** → **Section** → **Lesson** (abstract class with Video/Quiz/Reading subclasses)
- **User** with role-based security (admin, staff, instructor, student)
- **Enrollment** tracks student progress through courses
- **Quiz** system with questions/answers and submission tracking

## Key Patterns & Conventions

### Feature Module Structure
Each feature follows this pattern:
```
feature/{domain}/
├── {Domain}Controller.java
├── {Domain}Repository.java
├── {Domain}Service.java & {Domain}ServiceImpl.java
├── entity/{Domain}.java
├── dto/{Domain}Dto.java, {Domain}Request.java
├── mapper/{Domain}Mapper.java
└── constant/, enums/, exceptions/ as needed
```

### MapStruct Configuration
All mappers use Spring component model with consistent configuration:
```java
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
```

### Security & Authorization
- JWT-based authentication with OAuth2 resource server
- Role-based access control using `@HasAnyRole` annotations
- Resource-level authorization via `AuthorizationService` and AOP aspects
- Use `SecurityHelper.getCurrentUser()` to get authenticated user
- Authorization checks: `canAccessLesson()`, `canModifyCourse()`, etc.

### Entity Inheritance Patterns
- **Lesson** uses JPA `@Inheritance(strategy = InheritanceType.JOINED)` with discriminator
- **BaseEntity** provides audit fields (createdDate, updatedDate, createdBy, lastModifiedBy)
- All DTOs extend `BaseDto` for consistent audit field mapping

## Development Commands

### Build & Run
```bash
./mvnw spring-boot:run                    # Run application
./mvnw clean package                      # Build JAR
./mvnw test                               # Run tests
docker-compose -f docker-compose.dev.yml up  # Start dependencies (PostgreSQL, Redis, MinIO)
```

### Database
- PostgreSQL for main data storage
- Redis for caching and message queuing
- Hibernate with `ddl-auto: update` in dev profile

## Critical Implementation Details

### Lesson Type Factory Pattern
When creating lessons, use the factory method in `LessonMapper`:
```java
// Creates appropriate Video/Quiz/Reading entity based on LessonType
Lesson lesson = lessonMapper.toEntity(lessonDto);
```

### Email Templates with Thymeleaf
- HTML email templates in `src/main/resources/templates/email/`
- Fragment-based layout system (`layout.html`, `header.html`, `footer.html`)
- Context variables passed via `Context` object to template engine

### Transaction Boundaries
- Service methods use `@Transactional(readOnly = true)` for queries
- Write operations use `@Transactional` without readOnly flag
- Complex operations like course enrollment span multiple services

## Configuration Profiles

### Development (`application-dev.yml`)
- PostgreSQL on localhost:5432
- Redis on localhost:6379
- Detailed SQL logging enabled
- File upload to local directory

### Mail Configuration (`application-mail.yml`)
- Multiple email provider support with failover
- Thymeleaf template processing
- Background email sending via Redis queue

## Testing Data
The `TestingDataService` creates sample data from JSON files in `src/main/resources/data/`. It demonstrates proper entity relationship setup and lesson type creation patterns.

## Common Gotchas
- Always use `@Transactional` for operations that modify data
- Lesson entities require section assignment and author
- Course-instructor relationships managed via `CourseInstructor` join entity
- MapStruct requires Spring component model for dependency injection
- Security context available via `SecurityHelper` throughout the application

# Course Feature Refactoring Guidelines

## Current Architecture Issues

### 1. CourseServiceImpl Monolithic Structure
The `CourseServiceImpl` currently contains 582 lines with multiple responsibilities:
- Course CRUD operations
- Search and filtering logic
- Instructor authorization checks
- Section and lesson management
- Enrollment progress tracking
- Status management
- Notification handling

### 2. Excessive Dependencies
The service class has 15+ autowired dependencies, violating SRP (Single Responsibility Principle):
```java
@Autowired private CourseRepository courseRepository;
@Autowired private CategoryRepository categoryRepository;
@Autowired private SectionRepository sectionRepository;
@Autowired private EnrollmentRepository enrollmentRepository;
@Autowired private CourseInstructorRepository courseInstructorRepository;
@Autowired private UserProgressRepository lessonProgressRepository;
@Autowired private NotificationService notificationService;
// ... more dependencies
```

## Recommended Refactoring Strategy

### Phase 1: Extract Core Services

#### 1.1 CourseValidationService
**Purpose**: Handle all course-related validations
- Course slug uniqueness validation
- Course creation/update validation
- Instructor permission validation
- Course status transition validation

#### 1.2 CourseSearchService
**Purpose**: Handle search and filtering operations
- Course search by criteria
- Instructor course filtering
- Category-based filtering
- Published course retrieval

#### 1.3 CourseProgressService
**Purpose**: Handle course progress and statistics
- Calculate course completion rates
- Generate course statistics
- Track user progress in courses
- Enrollment progress management

#### 1.4 CourseNotificationService
**Purpose**: Handle course-related notifications
- Course status change notifications
- Instructor notifications
- Student enrollment notifications

### Phase 2: Implement Strategy Pattern

#### 2.1 Course Status Strategy
Create strategies for different course status operations:
- `DraftCourseStrategy` - Handle draft course operations
- `PublishedCourseStrategy` - Handle published course operations
- `ArchivedCourseStrategy` - Handle archived course operations

#### 2.2 Course Search Strategy
Implement different search strategies:
- `BasicCourseSearchStrategy` - Simple text-based search
- `AdvancedCourseSearchStrategy` - Multi-criteria search with filters
- `InstructorCourseSearchStrategy` - Instructor-specific search

### Phase 3: Refactored Structure

```
course/
├── service/
│   ├── CourseService.java                    # Main interface (simplified)
│   ├── CourseServiceImpl.java               # Main orchestrator (reduced complexity)
│   ├── validation/
│   │   ├── CourseValidationService.java
│   │   └── CourseValidationServiceImpl.java
│   ├── search/
│   │   ├── CourseSearchService.java
│   │   └── CourseSearchServiceImpl.java
│   ├── progress/
│   │   ├── CourseProgressService.java
│   │   └── CourseProgressServiceImpl.java
│   ├── notification/
│   │   ├── CourseNotificationService.java
│   │   └── CourseNotificationServiceImpl.java
│   └── strategy/
│       ├── status/
│       │   ├── CourseStatusStrategy.java
│       │   ├── DraftCourseStrategy.java
│       │   ├── PublishedCourseStrategy.java
│       │   └── ArchivedCourseStrategy.java
│       └── search/
│           ├── CourseSearchStrategy.java
│           ├── BasicCourseSearchStrategy.java
│           ├── AdvancedCourseSearchStrategy.java
│           └── InstructorCourseSearchStrategy.java
```

## Implementation Guidelines

### Service Extraction Rules
1. **Single Responsibility**: Each extracted service should have one clear purpose
2. **Dependency Injection**: Use constructor injection with `@RequiredArgsConstructor`
3. **Interface First**: Always create interfaces before implementations
4. **Transaction Boundaries**: Use `@Transactional` appropriately in each service

### Example: CourseValidationService

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseValidationServiceImpl implements CourseValidationService {
    
    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final SecurityHelper securityHelper;
    
    @Override
    public void validateCourseCreation(CourseRequest request) {
        validateSlugUniqueness(request.getSlug());
        validateCategoryExists(request.getCategoryId());
        validateInstructorPermissions();
    }
    
    @Override
    public void validateInstructorAccess(UUID courseId, UUID instructorId) {
        if (!courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, instructorId)) {
            throw new UnauthorizedException("Instructor does not have access to this course");
        }
    }
    
    private void validateSlugUniqueness(String slug) {
        if (courseRepository.existsBySlug(slug)) {
            throw new ValidationException("Course slug already exists: " + slug);
        }
    }
}
```

### Strategy Pattern Implementation

```java
@Component
public class CourseStatusStrategyFactory {
    
    private final Map<CourseStatus, CourseStatusStrategy> strategies;
    
    public CourseStatusStrategyFactory(List<CourseStatusStrategy> strategies) {
        this.strategies = strategies.stream()
            .collect(Collectors.toMap(
                CourseStatusStrategy::getSupportedStatus,
                Function.identity()
            ));
    }
    
    public CourseStatusStrategy getStrategy(CourseStatus status) {
        CourseStatusStrategy strategy = strategies.get(status);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for status: " + status);
        }
        return strategy;
    }
}
```

## Migration Strategy

### Step 1: Create Interfaces
- Define interfaces for all extracted services
- Keep existing CourseServiceImpl working during migration

### Step 2: Implement One Service at a Time
- Start with `CourseValidationService` (smallest scope)
- Test thoroughly before moving to next service
- Update `CourseServiceImpl` to delegate to new services

### Step 3: Refactor Controller Layer
- Update `CourseController` to use specific services when appropriate
- Maintain existing API contracts
- Add validation at controller level

### Step 4: Add Strategy Patterns
- Implement status strategies last
- Use factory pattern for strategy selection
- Ensure backward compatibility

## Testing Strategy

### Unit Testing
- Each extracted service should have comprehensive unit tests
- Mock dependencies using `@MockBean`
- Test error scenarios and edge cases

### Integration Testing
- Test service interactions
- Verify transaction boundaries work correctly
- Test with real database for repository interactions

### Example Test Structure
```java
@ExtendWith(MockitoExtension.class)
class CourseValidationServiceImplTest {
    
    @Mock
    private CourseRepository courseRepository;
    
    @Mock
    private SecurityHelper securityHelper;
    
    @InjectMocks
    private CourseValidationServiceImpl courseValidationService;
    
    @Test
    void shouldThrowExceptionWhenSlugExists() {
        // Given
        when(courseRepository.existsBySlug("existing-slug")).thenReturn(true);
        
        // When & Then
        assertThrows(ValidationException.class, 
            () -> courseValidationService.validateSlugUniqueness("existing-slug"));
    }
}
```

## Configuration Updates

### Add Service Configuration
```java
@Configuration
@ComponentScan(basePackages = "com.vinaacademy.platform.feature.course")
public class CourseServiceConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "course.validation.enabled", havingValue = "true", matchIfMissing = true)
    public CourseValidationService courseValidationService(
            CourseRepository courseRepository,
            SecurityHelper securityHelper) {
        return new CourseValidationServiceImpl(courseRepository, securityHelper);
    }
}
```

## Performance Considerations

### Caching Strategy
- Add `@Cacheable` to frequently accessed course data
- Use different cache regions for different data types
- Implement cache eviction on course updates

### Query Optimization
- Optimize course search queries with proper indexing
- Use pagination for large result sets
- Implement query result caching for complex searches

## Monitoring and Observability

### Metrics
- Add method-level metrics for each service
- Monitor service call patterns
- Track performance improvements post-refactoring

### Logging
- Add structured logging to each service
- Include correlation IDs for request tracing
- Log important business events

## Success Criteria

### Code Quality Metrics
- Reduce CourseServiceImpl from 582 to <200 lines
- Achieve >80% test coverage for all services
- Reduce cyclomatic complexity to <10 per method

### Performance Metrics
- Maintain or improve response times
- Reduce database query count where possible
- Improve cache hit rates

### Maintainability
- Each service should have <5 dependencies
- Clear separation of concerns
- Easy to add new features without modifying existing code
