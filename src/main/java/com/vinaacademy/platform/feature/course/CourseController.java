package com.vinaacademy.platform.feature.course;

import static com.vinaacademy.platform.feature.course.constants.CourseConstants.COURSE_DEFAULT_SIZE;

import com.vinaacademy.platform.feature.common.response.ApiResponse;
import com.vinaacademy.platform.feature.course.dto.*;
import com.vinaacademy.platform.feature.course.permission.CoursePermissionService;
import com.vinaacademy.platform.feature.course.service.CourseCommandService;
import com.vinaacademy.platform.feature.course.service.CourseQueryService;
import com.vinaacademy.platform.feature.user.auth.annotation.HasAnyRole;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courses")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(
    name = "Course API",
    description = "Course management operations including CRUD, search, and status management")
public class CourseController {

  private final CourseCommandService courseCommandService;
  private final CourseQueryService courseQueryService;
  private final CoursePermissionService coursePermissionService;
  private final SecurityHelper securityHelper;

  // ---- Course Details Endpoints ----

  @GetMapping("/by-slug/{slug}")
  @Operation(
      summary = "Get course details by slug",
      description =
          "Retrieves complete course information including instructors, sections, lessons, and reviews. This endpoint is cached for performance.",
      parameters = {
        @Parameter(
            name = "slug",
            description = "Unique course identifier",
            example = "spring-boot-masterclass")
      })
  public ApiResponse<CourseDetailsResponse> getCourseDetails(@PathVariable String slug) {
    log.debug("Getting course details for slug: {}", slug);
    return ApiResponse.success(courseQueryService.getCourseBySlug(slug));
  }

  @GetMapping("/details/by-id/{id}")
  @Operation(
      summary = "Get course details by ID",
      description =
          "Retrieves complete course information by ID including instructors, sections, lessons, and reviews. Used for internal operations.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      })
  public ApiResponse<CourseDetailsResponse> getCourseDetailsById(@PathVariable UUID id) {
    log.debug("Getting course details for id: {}", id);
    return ApiResponse.success(courseQueryService.getCourseById(id));
  }

  @GetMapping
  @Operation(
      summary = "Search courses with filters",
      description =
          "Searches courses with pagination and filtering options. Supports filtering by category, title, description, instructor name, and price range. Results are sorted and paginated.",
      parameters = {
        @Parameter(name = "title", description = "Course title filter", example = "Spring Boot"),
        @Parameter(
            name = "description",
            description = "Course description filter",
            example = "microservices"),
        @Parameter(
            name = "categorySlug",
            description = "Course category slug",
            example = "backend-development"),
        @Parameter(
            name = "instructorName",
            description = "Instructor name filter",
            example = "John Doe"),
        @Parameter(name = "minPrice", description = "Minimum price filter", example = "100000"),
        @Parameter(name = "maxPrice", description = "Maximum price filter", example = "2000000"),
        @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
        @Parameter(name = "size", description = "Page size", example = "10"),
        @Parameter(name = "sort", description = "Sort criteria", example = "name,asc")
      })
  public ApiResponse<Page<CourseDto>> searchPublishedCourses(
      @Valid @ModelAttribute CourseSearchRequest searchRequest,
      @PageableDefault(size = COURSE_DEFAULT_SIZE, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    Pageable resolved = resolveAndValidateSort(pageable);
    Page<CourseDto> coursePage = courseQueryService.searchPublishedCourses(searchRequest, resolved);
    log.debug(
        "Searched courses with criteria={}, page={}, size={}",
        searchRequest,
        resolved.getPageNumber(),
        resolved.getPageSize());
    return ApiResponse.success(coursePage);
  }

  @GetMapping("/details")
  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
  @Operation(
      summary = "Search course details (Admin/Staff only)",
      description =
          "Advanced search for course details with full information including private data. Only accessible by admins and staff members.",
      parameters = {
        @Parameter(name = "title", description = "Course title filter", example = "Spring Boot"),
        @Parameter(
            name = "description",
            description = "Course description filter",
            example = "microservices"),
        @Parameter(
            name = "categorySlug",
            description = "Course category slug",
            example = "backend-development"),
        @Parameter(
            name = "instructorName",
            description = "Instructor name filter",
            example = "John Doe"),
        @Parameter(name = "minPrice", description = "Minimum price filter", example = "100000"),
        @Parameter(name = "maxPrice", description = "Maximum price filter", example = "2000000"),
        @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
        @Parameter(name = "size", description = "Page size", example = "10"),
        @Parameter(name = "sort", description = "Sort criteria", example = "name,asc")
      })
  public ApiResponse<Page<CourseDetailsResponse>> searchCoursesDetail(
      @Valid @ModelAttribute CourseSearchRequest searchRequest,
      @PageableDefault(size = COURSE_DEFAULT_SIZE, sort = "name", direction = Sort.Direction.ASC)
          Pageable pageable) {
    Pageable resolved = resolveAndValidateSort(pageable);
    Page<CourseDetailsResponse> coursePage =
        courseQueryService.searchCourseDetails(searchRequest, resolved);
    log.debug(
        "Searched course details with criteria={}, page={}, size={}",
        searchRequest,
        resolved.getPageNumber(),
        resolved.getPageSize());
    return ApiResponse.success(coursePage);
  }

  @HasAnyRole({AuthConstants.STAFF_ROLE, AuthConstants.ADMIN_ROLE})
  @GetMapping("/statuscount")
  @Operation(
      summary = "Get course count by status (Admin/Staff only)",
      description =
          "Retrieves the count of courses grouped by their status. Useful for dashboard analytics and administrative reporting.")
  public ApiResponse<CourseCountStatusDto> getCourseCountByStatus() {
    log.debug("Getting course count by status");
    return ApiResponse.success(courseQueryService.getCountCourses());
  }

  // ---- Course Management Endpoints ----

  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE, AuthConstants.STAFF_ROLE})
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create a new course",
      description =
          "Creates a new course in DRAFT status. Only instructors, admins, and staff can create courses.",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Course created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid course data"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions")
      })
  public ApiResponse<CourseDto> createCourse(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Course creation data",
              content =
                  @Content(
                      schema = @Schema(implementation = CourseRequest.class),
                      examples =
                          @ExampleObject(
                              value =
                                  """
                                            {
                                              "name": "Spring Boot Masterclass",
                                              "slug": "spring-boot-masterclass",
                                              "description": "Complete guide to Spring Boot development",
                                              "categorySlug": "programming",
                                              "image": "https://example.com/image.jpg",
                                              "language": "Vietnamese",
                                              "level": "INTERMEDIATE",
                                              "price": 299.99
                                            }
                                            """)))
          @RequestBody
          @Valid
          CourseRequest request) {
    log.debug("Creating course with name: {}", request.getName());
    return ApiResponse.success(courseCommandService.createCourse(request));
  }

  @PatchMapping("/by-id/{id}/status")
  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE})
  @Operation(
      summary = "Update course status (Admin/Staff only)",
      description =
          "Updates the status of a course. Only admins and staff can change course status between DRAFT, PENDING_APPROVAL, PUBLISHED, and ARCHIVED states.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Course status update request",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = CourseStatusRequest.class),
                      examples =
                          @ExampleObject(
                              name = "Publish course example",
                              value =
                                  """
                                            {
                                                "status": "PUBLISHED"
                                            }
                                            """))))
  public ApiResponse<Boolean> updateStatusCourse(
      @PathVariable UUID id, @RequestBody @Valid CourseStatusRequest courseStatusRequest) {
    Boolean updated = courseCommandService.updateStatusCourse(id, courseStatusRequest.getStatus());
    log.debug(
        "Updated course status for id={} to status={}, result={}",
        id,
        courseStatusRequest.getStatus(),
        updated);
    return ApiResponse.success(updated);
  }

  @HasAnyRole({AuthConstants.ADMIN_ROLE, AuthConstants.INSTRUCTOR_ROLE})
  @DeleteMapping("/by-id/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(
      summary = "Delete course",
      description =
          "Deletes a course permanently. Instructors can only delete their own courses that have no active enrollments. Admins can delete any course regardless of enrollments.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      })
  public ApiResponse<Void> deleteCourse(@PathVariable UUID id) {
    log.debug("Deleting course with id: {}", id);
    courseCommandService.deleteCourse(id);
    return ApiResponse.success("course.delete.success");
  }

  @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
  @PutMapping("/by-id/{id}")
  @Operation(
      summary = "Update course (Instructor only)",
      description =
          "Updates an existing course. Only the course instructor can update their course. The slug will be automatically regenerated if the title changes.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      },
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Course update request",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = CourseRequest.class),
                      examples =
                          @ExampleObject(
                              name = "Update course example",
                              value =
                                  """
                                            {
                                                "title": "Advanced Spring Boot Masterclass",
                                                "description": "Comprehensive course covering advanced Spring Boot concepts and microservices architecture",
                                                "price": 1500000,
                                                "level": "ADVANCED",
                                                "categorySlug": "backend-development",
                                                "shortDescription": "Master advanced Spring Boot patterns and best practices",
                                                "whatYouWillLearn": ["Microservices architecture", "Spring Security", "Advanced testing"],
                                                "requirements": ["Basic Spring Boot knowledge", "Java 11+"]
                                            }
                                            """))))
  public ApiResponse<CourseDto> updateCourse(
      @PathVariable UUID id, @RequestBody @Valid CourseRequest request) {
    log.debug("Updating course with id: {}", id);
    return ApiResponse.success(courseCommandService.updateCourse(id, request));
  }

  @GetMapping("/by-slug/{slug}/learning")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get course learning details (Authenticated users only)",
      description =
          "Retrieves course information optimized for learning experience. Includes progress tracking and learning-specific data for enrolled users.",
      parameters = {
        @Parameter(
            name = "slug",
            description = "Unique course identifier",
            example = "spring-boot-masterclass")
      })
  public ApiResponse<CourseDto> getCourseLearning(@PathVariable String slug) {
    log.debug("Getting course learning information for slug: {}", slug);
    return ApiResponse.success(courseQueryService.getCourseLearning(slug));
  }

  @GetMapping("/by-id/{id}/learning")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get course learning details by ID (Authenticated users only)",
      description =
          "Retrieves course information by ID optimized for learning experience. Includes progress tracking and learning-specific data for enrolled users.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      })
  public ApiResponse<CourseDto> getCourseLearningById(@PathVariable UUID id) {
    log.debug("Getting course learning information for id: {}", id);
    return ApiResponse.success(courseQueryService.getCourseLearningById(id));
  }

  @GetMapping("/by-id/{id}")
  @Operation(
      summary = "Get course by ID",
      description =
          "Retrieves course information by internal UUID. Primarily used for internal system operations and integrations.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      })
  public ApiResponse<CourseDto> getCourseById(@PathVariable UUID id) {
    log.debug("Getting course information for id: {}", id);
    return ApiResponse.success(courseQueryService.getCourseInfoById(id));
  }

  @GetMapping("/instructor/courses")
  @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
  @Operation(
      summary = "Search instructor's courses (Instructor only)",
      description =
          "Searches courses belonging to the current instructor with filtering options. Only returns courses created by the authenticated instructor.",
      parameters = {
        @Parameter(name = "title", description = "Course title filter", example = "Spring Boot"),
        @Parameter(
            name = "description",
            description = "Course description filter",
            example = "microservices"),
        @Parameter(
            name = "categorySlug",
            description = "Course category slug",
            example = "backend-development"),
        @Parameter(name = "page", description = "Page number (0-based)", example = "0"),
        @Parameter(name = "size", description = "Page size", example = "10"),
        @Parameter(name = "sort", description = "Sort criteria", example = "createdDate,desc")
      })
  public ApiResponse<Page<CourseDto>> searchInstructorCourses(
      @ModelAttribute CourseSearchRequest searchRequest,
      @PageableDefault(
              size = COURSE_DEFAULT_SIZE,
              sort = "createdDate",
              direction = Sort.Direction.DESC)
          Pageable pageable) {
    User currentUser = securityHelper.getCurrentUser();
    Pageable resolved = resolveAndValidateSort(pageable);
    Page<CourseDto> coursePage =
        courseQueryService.searchInstructorCourses(currentUser.getId(), searchRequest, resolved);
    log.debug(
        "Searched instructor courses for user={}, criteria={}, page={}",
        currentUser.getId(),
        searchRequest,
        resolved.getPageNumber());
    return ApiResponse.success(coursePage);
  }

  @PostMapping("/by-id/{id}/submit-for-review")
  @HasAnyRole({AuthConstants.INSTRUCTOR_ROLE})
  @Operation(
      summary = "Submit course for review (Instructor only)",
      description =
          "Submits a course for admin/staff review. Changes the course status from DRAFT to PENDING_APPROVAL. Only the course owner can submit their course for review.",
      parameters = {
        @Parameter(
            name = "id",
            description = "Course UUID",
            example = "550e8400-e29b-41d4-a716-446655440000")
      })
  public ApiResponse<Boolean> submitCourseForReview(@PathVariable UUID id) {
    User currentUser = securityHelper.getCurrentUser();

    // Validate permission using permission service
    coursePermissionService.validateCourseModifyPermission(id, currentUser.getId());

    Boolean result = courseCommandService.submitCourseForReview(id);
    log.info("Submitted course for review: courseId={}, userId={}", id, currentUser.getId());
    return ApiResponse.success(result);
  }

  // ---- helpers ----
  private static final Map<String, String> SORT_WHITELIST =
      Map.of(
          "name", "name",
          "createdDate", "createdDate",
          "updatedDate", "updatedDate",
          "rating", "rating");

  private Pageable resolveAndValidateSort(Pageable pageable) {
    Sort mapped =
        Sort.by(
            pageable.getSort().stream()
                .map(
                    order -> {
                      String property = order.getProperty();
                      String mappedProp = SORT_WHITELIST.get(property);
                      if (mappedProp == null) {
                        mappedProp = SORT_WHITELIST.values().iterator().next();
                      }
                      return new Sort.Order(order.getDirection(), mappedProp);
                    })
                .toList());
    return org.springframework.data.domain.PageRequest.of(
        pageable.getPageNumber(), pageable.getPageSize(), mapped);
  }
}
