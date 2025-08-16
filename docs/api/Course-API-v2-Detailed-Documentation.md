# Course API v2.0 - Detailed Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication & Authorization](#authentication--authorization)
3. [Data Models](#data-models)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Rate Limiting & Caching](#rate-limiting--caching)
7. [Examples](#examples)

## Overview

The Course API provides comprehensive functionality for managing online courses in the VinaAcademy platform. This RESTful API supports course creation, management, search, and status workflows with role-based access control.

**Base URL:** `/api/v1/courses`
**Version:** 2.0
**Content-Type:** `application/json`
**Authentication:** Bearer Token (JWT)

### Key Features
- ✅ Course lifecycle management (DRAFT → PENDING → PUBLISHED)
- ✅ Role-based permissions (Admin, Staff, Instructor, Student)
- ✅ Advanced search and filtering
- ✅ Instructor course management
- ✅ Caching and performance optimization
- ✅ Comprehensive validation and error handling

## Authentication & Authorization

### Authentication
All endpoints require Bearer token authentication except where marked as "Public Access".

```http
Authorization: Bearer <JWT_TOKEN>
```

### Role-Based Access Control

| Role | Permissions |
|------|-------------|
| **ADMIN** | Full access to all operations |
| **STAFF** | Course approval, status management, advanced search |
| **INSTRUCTOR** | Create, update, delete own courses; submit for review |
| **STUDENT** | View published courses, access learning content |

## Data Models

### CourseRequest
Used for creating and updating courses.

```typescript
{
  "name": string,                    // Required. Course title
  "description": string,             // Optional. Detailed description
  "slug": string,                    // Optional. Auto-generated from name if not provided
  "price": number,                   // Required. Min value: 0
  "level": CourseLevel,              // Required. BEGINNER|INTERMEDIATE|ADVANCED
  "language": string,                // Required. Course language
  "categorySlug": string,            // Required. Valid category identifier
  "image": string,                   // Optional. Course thumbnail URL
  "status": CourseStatus             // Optional. Only for admin operations
}
```

### CourseDto
Basic course information response.

```typescript
{
  "id": UUID,
  "name": string,
  "description": string,
  "slug": string,
  "price": number,
  "level": CourseLevel,
  "status": CourseStatus,
  "language": string,
  "categoryName": string,
  "rating": number,                  // Average rating (0-5)
  "totalRating": number,             // Total number of ratings
  "totalStudent": number,            // Total enrolled students
  "totalSection": number,            // Number of course sections
  "totalLesson": number,             // Total lessons in course
  "image": string,
  "createdDate": datetime,
  "updatedDate": datetime,
  "progress": EnrollmentProgressDto, // Only for authenticated users
  "sections": SectionDto[]           // Only in learning context
}
```

### CourseDetailsResponse
Comprehensive course information with related data.

```typescript
{
  ...CourseDto,                      // All fields from CourseDto
  "instructors": UserDto[],          // All course instructors
  "ownerInstructor": UserDto,        // Primary course owner
  "sections": SectionDto[],          // Course sections with lessons
  "reviews": CourseReviewDto[],      // Course reviews and ratings
  "categorySlug": string             // Category identifier
}
```

### CourseSearchRequest
Parameters for searching and filtering courses.

```typescript
{
  "keyword": string,                 // Search in title and description
  "categorySlug": string,            // Filter by category
  "instructorId": UUID,              // Filter by instructor
  "level": CourseLevel,              // Filter by difficulty level
  "language": string,                // Filter by language
  "minPrice": number,                // Minimum price filter
  "maxPrice": number,                // Maximum price filter
  "minRating": number,               // Minimum rating filter (0-5)
  "status": CourseStatus             // Filter by status (admin only)
}
```

### CourseStatusRequest
For updating course status.

```typescript
{
  "status": CourseStatus             // DRAFT|PENDING|PUBLISHED|REJECTED
}
```

### Enums

#### CourseLevel
```typescript
enum CourseLevel {
  BEGINNER = "BEGINNER",
  INTERMEDIATE = "INTERMEDIATE", 
  ADVANCED = "ADVANCED"
}
```

#### CourseStatus
```typescript
enum CourseStatus {
  DRAFT = "DRAFT",                   // Initial state, editable by instructor
  PENDING = "PENDING",               // Submitted for review
  PUBLISHED = "PUBLISHED",           // Approved and publicly available
  REJECTED = "REJECTED"              // Rejected, needs revision
}
```

## API Endpoints

### 1. Course Management

#### 1.1 Create Course
Creates a new course in DRAFT status.

```http
POST /api/v1/courses
```

**Authorization:** Admin, Staff, Instructor
**Request Body:** [CourseRequest](#courserequest)

**Example Request:**
```json
{
  "name": "Spring Boot Masterclass",
  "description": "Complete guide to Spring Boot development with microservices",
  "categorySlug": "backend-development",
  "price": 1999000,
  "level": "INTERMEDIATE",
  "language": "Vietnamese"
}
```

**Example Response:**
```json
{
  "status": "SUCCESS",
  "message": "Course created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Spring Boot Masterclass",
    "slug": "spring-boot-masterclass",
    "status": "DRAFT",
    "price": 1999000,
    "level": "INTERMEDIATE",
    "language": "Vietnamese",
    "categoryName": "Backend Development",
    "rating": 0,
    "totalRating": 0,
    "totalStudent": 0,
    "totalSection": 0,
    "totalLesson": 0,
    "createdDate": "2025-08-16T10:30:00Z",
    "updatedDate": "2025-08-16T10:30:00Z"
  }
}
```

#### 1.2 Update Course
Updates an existing course. Only course owner can update.

```http
PUT /api/v1/courses/by-id/{id}
```

**Authorization:** Instructor (owner only)
**Path Parameters:**
- `id` (UUID): Course identifier

**Request Body:** [CourseRequest](#courserequest)

**Business Rules:**
- Only course owner can update
- Course must be in DRAFT or REJECTED status
- Slug is auto-regenerated if title changes

#### 1.3 Delete Course
Permanently deletes a course.

```http
DELETE /api/v1/courses/by-id/{id}
```

**Authorization:** Admin (any course), Instructor (own course only)
**Path Parameters:**
- `id` (UUID): Course identifier

**Business Rules:**
- Instructors can only delete courses with no active enrollments
- Admins can delete any course regardless of enrollments
- Returns 204 No Content on success

### 2. Course Queries

#### 2.1 Get Course Details by Slug
Retrieves comprehensive course information by slug.

```http
GET /api/v1/courses/by-slug/{slug}
```

**Authorization:** Public access
**Path Parameters:**
- `slug` (string): Unique course identifier

**Response:** [CourseDetailsResponse](#coursedetailsresponse)

**Caching:** 1 hour cache duration

#### 2.2 Get Course Details by ID
Retrieves comprehensive course information by ID.

```http
GET /api/v1/courses/details/by-id/{id}
```

**Authorization:** Public access
**Path Parameters:**
- `id` (UUID): Course identifier

**Response:** [CourseDetailsResponse](#coursedetailsresponse)

#### 2.3 Get Course Basic Info by ID
Retrieves basic course information by ID.

```http
GET /api/v1/courses/by-id/{id}
```

**Authorization:** Public access
**Path Parameters:**
- `id` (UUID): Course identifier

**Response:** [CourseDto](#coursedto)

#### 2.4 Search Published Courses
Searches and filters published courses with pagination.

```http
GET /api/v1/courses
```

**Authorization:** Public access
**Query Parameters:**
- All parameters from [CourseSearchRequest](#coursesearchrequest)
- Standard pagination parameters

**Example Request:**
```http
GET /api/v1/courses?title=Spring&categorySlug=backend-development&minPrice=100000&maxPrice=2000000&page=0&size=10&sort=name,asc
```

**Response:**
```json
{
  "status": "SUCCESS",
  "data": {
    "content": [CourseDto[], ...],
    "totalElements": 45,
    "totalPages": 5,
    "number": 0,
    "size": 10,
    "numberOfElements": 10,
    "first": true,
    "last": false,
    "empty": false
  }
}
```

#### 2.5 Search Course Details (Admin/Staff)
Advanced search with full course details including private information.

```http
GET /api/v1/courses/details
```

**Authorization:** Admin, Staff
**Query Parameters:** Same as public search + status filtering

**Response:** Paginated [CourseDetailsResponse](#coursedetailsresponse)

### 3. Learning Context

#### 3.1 Get Course Learning Info by Slug
Retrieves course information optimized for learning experience.

```http
GET /api/v1/courses/by-slug/{slug}/learning
```

**Authorization:** Authenticated users
**Path Parameters:**
- `slug` (string): Course identifier

**Features:**
- Includes user progress information
- Shows completion status
- Optimized for learning interface

#### 3.2 Get Course Learning Info by ID
Same as above but by course ID.

```http
GET /api/v1/courses/by-id/{id}/learning
```

**Authorization:** Authenticated users

### 4. Instructor Operations

#### 4.1 Search Instructor Courses
Retrieves courses belonging to the authenticated instructor.

```http
GET /api/v1/courses/instructor/courses
```

**Authorization:** Instructor
**Query Parameters:**
- Search and filter parameters
- Pagination parameters

**Default Sort:** `createdDate,desc`

#### 4.2 Submit Course for Review
Submits a course for admin/staff review.

```http
POST /api/v1/courses/by-id/{id}/submit-for-review
```

**Authorization:** Instructor (owner only)
**Path Parameters:**
- `id` (UUID): Course identifier

**Business Rules:**
- Course must be in DRAFT status
- Course must have content (sections/lessons)
- Only course owner can submit

**Response:**
```json
{
  "status": "SUCCESS",
  "message": "Course submitted for review successfully",
  "data": true
}
```

### 5. Administrative Operations

#### 5.1 Update Course Status
Changes the status of a course.

```http
PATCH /api/v1/courses/by-id/{id}/status
```

**Authorization:** Admin, Staff
**Path Parameters:**
- `id` (UUID): Course identifier

**Request Body:**
```json
{
  "status": "PUBLISHED"
}
```

**Valid Status Transitions:**
- `DRAFT` → `PENDING` (via submit for review)
- `PENDING` → `PUBLISHED` (approval)
- `PENDING` → `REJECTED` (rejection)
- `REJECTED` → `PENDING` (resubmission)

## Error Handling

### Standard Error Response Format
```json
{
  "status": "ERROR",
  "code": 400,
  "message": "Human-readable error message",
  "details": "Optional additional details",
  "timestamp": "2025-08-16T10:30:00Z",
  "path": "/api/v1/courses"
}
```

### Common Error Codes

| HTTP Code | Error Type | Description |
|-----------|------------|-------------|
| 400 | Bad Request | Invalid request data or business rule violation |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions for the operation |
| 404 | Not Found | Requested resource does not exist |
| 409 | Conflict | Resource conflict (e.g., duplicate slug) |
| 422 | Unprocessable Entity | Validation errors |
| 500 | Internal Server Error | Server-side error |

### Specific Course Error Messages

| Error Key | HTTP Code | Description |
|-----------|-----------|-------------|
| `course.not_found` | 404 | Course with specified ID/slug not found |
| `course.not_published` | 400 | Course is not in published status |
| `course.permission.modify_denied` | 403 | User lacks permission to modify course |
| `course.permission.delete_denied` | 403 | User lacks permission to delete course |
| `course.has_active_enrollments` | 400 | Cannot delete course with active enrollments |
| `course.status.invalid_transition` | 400 | Invalid status transition attempted |
| `course.slug.already_exists` | 409 | Course slug already exists |
| `course.validation.name_required` | 422 | Course name is required |
| `course.validation.category_invalid` | 422 | Invalid category specified |

## Rate Limiting & Caching

### Rate Limiting
- **Public endpoints:** 100 requests/minute per IP
- **Authenticated endpoints:** 1000 requests/minute per user
- **Admin endpoints:** 5000 requests/minute per user

### Caching Strategy
| Endpoint | Cache Duration | Cache Key |
|----------|----------------|-----------|
| Course details by slug | 1 hour | `course:slug:{slug}` |
| Course details by ID | 1 hour | `course:id:{id}` |
| Course search results | 15 minutes | `course:search:{hash}` |
| Course existence checks | 30 minutes | `course:exists:{id}` |

**Cache Invalidation:** Automatic invalidation occurs when courses are modified.

## Pagination & Sorting

### Pagination Parameters
- `page` (int): Page number, 0-based (default: 0)
- `size` (int): Page size, 1-100 (default: 10)
- `sort` (string): Sort criteria in format `field,direction`

### Allowed Sort Fields
- `name` - Course name
- `createdDate` - Creation date
- `updatedDate` - Last modification date  
- `rating` - Average rating

### Sort Examples
- `sort=name,asc` - Sort by name ascending
- `sort=createdDate,desc` - Sort by creation date descending
- `sort=rating,desc&sort=name,asc` - Multi-field sorting

## Examples

### Complete Course Creation Flow

1. **Create Course:**
```bash
curl -X POST "https://api.vinaacademy.com/api/v1/courses" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Advanced React Development",
    "description": "Learn advanced React patterns and best practices",
    "categorySlug": "frontend-development",
    "price": 2500000,
    "level": "ADVANCED",
    "language": "Vietnamese"
  }'
```

2. **Add Content (sections/lessons)**
3. **Submit for Review:**
```bash
curl -X POST "https://api.vinaacademy.com/api/v1/courses/by-id/{courseId}/submit-for-review" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

4. **Admin Approval:**
```bash
curl -X PATCH "https://api.vinaacademy.com/api/v1/courses/by-id/{courseId}/status" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status": "PUBLISHED"}'
```

### Advanced Search Example

```bash
curl -G "https://api.vinaacademy.com/api/v1/courses" \
  --data-urlencode "keyword=Spring Boot" \
  --data-urlencode "categorySlug=backend-development" \
  --data-urlencode "level=INTERMEDIATE" \
  --data-urlencode "minPrice=500000" \
  --data-urlencode "maxPrice=3000000" \
  --data-urlencode "minRating=4.0" \
  --data-urlencode "page=0" \
  --data-urlencode "size=20" \
  --data-urlencode "sort=rating,desc"
```

### Instructor Dashboard Integration

```javascript
// Get instructor's courses
const response = await fetch('/api/v1/courses/instructor/courses?page=0&size=10&sort=createdDate,desc', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});

const { data } = await response.json();
console.log(`Total courses: ${data.totalElements}`);
console.log(`Courses:`, data.content);
```

## Internationalization

The API supports Vietnamese and English localization:

```http
Accept-Language: vi
# or
Accept-Language: en
```

Error messages and enum values are automatically localized based on the Accept-Language header.

## Webhook Events

The following events are triggered for course operations:

- `course.created` - New course created
- `course.updated` - Course modified
- `course.status_changed` - Course status transition
- `course.submitted_for_review` - Course submitted for approval
- `course.published` - Course published
- `course.deleted` - Course deleted

## SDK Examples

### JavaScript/TypeScript
```typescript
import { CourseAPI } from '@vinaacademy/api-client';

const courseApi = new CourseAPI({ 
  baseURL: 'https://api.vinaacademy.com',
  apiKey: 'your-api-key'
});

// Create course
const newCourse = await courseApi.createCourse({
  name: 'Node.js Fundamentals',
  categorySlug: 'backend-development',
  level: 'BEGINNER',
  price: 999000,
  language: 'Vietnamese'
});

// Search courses
const courses = await courseApi.searchCourses({
  keyword: 'React',
  level: 'INTERMEDIATE',
  page: 0,
  size: 10
});
```

### Java Spring Boot
```java
@Service
public class CourseService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public CourseDto createCourse(CourseRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<CourseRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<CourseDto>> response = restTemplate.exchange(
            "/api/v1/courses",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<ApiResponse<CourseDto>>() {}
        );
        
        return response.getBody().getData();
    }
}
```

---

**API Version:** 2.0  
**Last Updated:** August 16, 2025  
**Support:** api-support@vinaacademy.com
