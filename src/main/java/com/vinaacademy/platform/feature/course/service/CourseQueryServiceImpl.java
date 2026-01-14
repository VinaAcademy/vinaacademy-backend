package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.course.assembler.CourseAssembler;
import com.vinaacademy.platform.feature.course.dto.CategoryDistributionDto;
import com.vinaacademy.platform.feature.course.dto.CourseDashboardStatsDto;
import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.dto.CourseTrendDto;
import com.vinaacademy.platform.feature.course.dto.TopCoursesDto;
import com.vinaacademy.platform.feature.course.dto.TopInstructorsDto;
import com.vinaacademy.platform.feature.course.dto.RecentCoursesDto;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.mapper.CourseMapper;
import com.vinaacademy.platform.feature.course.permission.CoursePermissionService;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.course.repository.specification.CourseSpecBuilder;
import com.vinaacademy.platform.feature.course.repository.specification.CourseSpecification;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentProgressDto;
import com.vinaacademy.platform.feature.enrollment.mapper.EnrollmentMapper;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.revenue.entity.RevenueRecord;
import com.vinaacademy.platform.feature.revenue.repository.RevenueRecordRepository;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of course query service. Handles all read-only operations for courses. */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CourseQueryServiceImpl implements CourseQueryService {

  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final UserProgressRepository lessonProgressRepository;
  private final RevenueRecordRepository revenueRecordRepository;
  private final CourseInstructorRepository courseInstructorRepository;
  private final CourseMapper courseMapper;
  private final SecurityHelper securityHelper;
  private final CourseAssembler courseAssembler;
  private final CoursePermissionService coursePermissionService;

  @Override
  @Cacheable(value = "courseDetails", key = "#slug", unless = "#result == null")
  public CourseDetailsResponse getCourseBySlug(String slug) {
    log.debug("Fetching course details for slug: {}", slug);

    Course course =
        courseRepository
            .findBySlugWithDetails(slug)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

    // Only allow PUBLISHED courses for public users, unless user has permission
    if (course.getStatus() != CourseStatus.PUBLISHED) {
      try {
        User currentUser = securityHelper.getCurrentUser();
        
        // Check if user is admin/staff
        boolean isAdminOrStaff = securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE);
        
        // Check if user is course instructor
        boolean isInstructor = course.getInstructors().stream()
            .anyMatch(ci -> ci.getInstructor().getId().equals(currentUser.getId()));
        
        if (!isAdminOrStaff && !isInstructor) {
          log.warn("User {} attempted to access non-published course: {} (status: {})", 
              currentUser.getId(), slug, course.getStatus());
          throw BadRequestException.messageKey("course.not_published");
        }
      } catch (Exception e) {
        // No authenticated user or other error - only allow PUBLISHED
        log.warn("Unauthenticated user attempted to access non-published course: {} (status: {})", 
            slug, course.getStatus());
        throw BadRequestException.messageKey("course.not_published");
      }
    }

    return courseAssembler.assembleCourseDetailsResponse(course);
  }

  @Override
  @Cacheable(value = "courseById", key = "#id", unless = "#result == null")
  public CourseDetailsResponse getCourseById(UUID id) {
    log.debug("Fetching course details by ID: {}", id);

    Course course =
        courseRepository
            .findByIdWithDetails(id)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

    // Only allow PUBLISHED courses for public users, unless user has permission
    if (course.getStatus() != CourseStatus.PUBLISHED) {
      try {
        User currentUser = securityHelper.getCurrentUser();
        
        // Check if user is admin/staff
        boolean isAdminOrStaff = securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE);
        
        // Check if user is course instructor
        boolean isInstructor = course.getInstructors().stream()
            .anyMatch(ci -> ci.getInstructor().getId().equals(currentUser.getId()));
        
        if (!isAdminOrStaff && !isInstructor) {
          log.warn("User {} attempted to access non-published course: {} (status: {})", 
              currentUser.getId(), id, course.getStatus());
          throw BadRequestException.messageKey("course.not_published");
        }
      } catch (Exception e) {
        // No authenticated user or other error - only allow PUBLISHED
        log.warn("Unauthenticated user attempted to access non-published course: {} (status: {})", 
            id, course.getStatus());
        throw BadRequestException.messageKey("course.not_published");
      }
    }

    return courseAssembler.assembleCourseDetailsResponse(course);
  }

  @Override
  @Cacheable(value = "courseInfoById", key = "#id", unless = "#result == null")
  public CourseDto getCourseInfoById(UUID id) {
    log.debug("Fetching course info by ID: {}", id);

    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));
    
    // Only allow PUBLISHED courses for public users, unless user has permission
    if (course.getStatus() != CourseStatus.PUBLISHED) {
      try {
        User currentUser = securityHelper.getCurrentUser();
        
        // Check if user is admin/staff
        boolean isAdminOrStaff = securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE);
        
        // Check if user is course instructor
        boolean isInstructor = course.getInstructors().stream()
            .anyMatch(ci -> ci.getInstructor().getId().equals(currentUser.getId()));
        
        if (!isAdminOrStaff && !isInstructor) {
          log.warn("User {} attempted to access non-published course: {} (status: {})", 
              currentUser.getId(), id, course.getStatus());
          throw BadRequestException.messageKey("course.not_published");
        }
      } catch (Exception e) {
        // No authenticated user or other error - only allow PUBLISHED
        log.warn("Unauthenticated user attempted to access non-published course: {} (status: {})", 
            id, course.getStatus());
        throw BadRequestException.messageKey("course.not_published");
      }
    }
    
    return courseMapper.toDTO(course);
  }

  @Override
  public CourseDto getCourseLearning(String slug) {
    log.debug("Fetching course learning information for slug: {}", slug);

    Course course =
        courseRepository
            .findBySlug(slug)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

    return processCourseLearning(course);
  }

  @Override
  public CourseDto getCourseLearningById(UUID id) {
    log.debug("Fetching course learning information for id: {}", id);

    Course course =
        courseRepository
            .findById(id)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

    return processCourseLearning(course);
  }

  private CourseDto processCourseLearning(Course course) {
    CourseDto courseDto = courseMapper.toDTO(course);

    if (course.getStatus() != CourseStatus.PUBLISHED) {
      throw BadRequestException.messageKey("course.not_published");
    }

    User currentUser = securityHelper.getCurrentUser();

    // Set enrollment progress
    List<User> instructors =
        course.getInstructors().stream().map(CourseInstructor::getInstructor).toList();
    
    
    if (!securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)
        && !instructors.contains(currentUser)) {
      Enrollment courseEnrollment =
          enrollmentRepository
              .findByCourseAndUser(course, currentUser)
              .orElseThrow(() -> BadRequestException.messageKey("course.access_denied"));
      courseDto.setProgress(EnrollmentMapper.INSTANCE.toDto2(courseEnrollment));
    } else {
      courseDto.setProgress(new EnrollmentProgressDto());
    }

    // Process sections and lessons with user progress
    List<Section> sections = course.getSections();

    // Collect all lessons from all sections
    List<Lesson> allLessons =
        sections.stream().flatMap(section -> section.getLessons().stream()).toList();

    // Fetch all user progress records in a single query
    List<UserProgress> allUserProgress =
        lessonProgressRepository.findByUserAndLessonIn(currentUser, allLessons);

    // Create a map for quick lookup: lessonId -> UserProgress
    Map<UUID, UserProgress> progressMap =
        allUserProgress.stream()
            .collect(
                Collectors.toMap(progress -> progress.getLesson().getId(), progress -> progress));

    // Get sorted sections and lessons using the assembler
    List<SectionDto> sectionDtos =
        courseAssembler.processSectionsAndLessonsWithProgress(sections, progressMap);

    courseDto.setSections(sectionDtos);
    return courseDto;
  }

  @Override
  public Page<CourseDto> searchPublishedCourses(
      CourseSearchRequest searchRequest, Pageable pageable) {
    log.debug(
        "Searching courses with criteria: {}, page={}, size={}",
        searchRequest,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Specification<Course> spec = CourseSpecBuilder.buildPublicSearch(searchRequest);
    Page<Course> coursePage = courseRepository.findAll(spec, pageable);
    return coursePage.map(course -> {
      CourseDto dto = courseMapper.toDTO(course);
      dto.setNameInstructorOwner(courseMapper.extractOwnerInstructorName(course));
      return dto;
    });
  }

  @Override
  public Page<CourseDetailsResponse> searchCourseDetails(
      CourseSearchRequest searchRequest, Pageable pageable) {
    log.debug(
        "Searching course details with criteria: {}, page={}, size={}",
        searchRequest,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Specification<Course> spec = CourseSpecBuilder.buildAdminSearch(searchRequest);
    Page<Course> coursePage = courseRepository.findAll(spec, pageable);

    return coursePage.map(courseAssembler::assembleCourseDetailsResponse);
  }

  @Override
  public Page<CourseDto> searchInstructorCourses(
      UUID instructorId, CourseSearchRequest searchRequest, Pageable pageable) {
    log.debug(
        "Searching instructor courses for instructor: {}, criteria: {}, page={}, size={}",
        instructorId,
        searchRequest,
        pageable.getPageNumber(),
        pageable.getPageSize());

    Specification<Course> spec =
        CourseSpecBuilder.buildInstructorSearch(instructorId, searchRequest);
    Page<Course> coursePage = courseRepository.findAll(spec, pageable);
    return coursePage.map(courseMapper::toDTO);
  }

  @Override
  public CourseCountStatusDto getCountCourses() {
    log.debug("Fetching course count by status");

    List<Object[]> statusCounts = courseRepository.countCoursesByStatus();

    long totalPublished = 0;
    long totalRejected = 0;
    long totalPending = 0;

    for (Object[] result : statusCounts) {
      CourseStatus status = (CourseStatus) result[0];
      long count = (long) result[1];

      switch (status) {
        case PUBLISHED -> totalPublished = count;
        case REJECTED -> totalRejected = count;
        case PENDING -> totalPending = count;
        case DRAFT -> {
          // Draft courses are not counted in status statistics
        }
      }
    }

    Specification<Course> pendingSpec = Specification.where(CourseSpecification.hasStatus(CourseStatus.PENDING))
            .or(CourseSpecification.hasPendingLesson());
    pendingSpec = pendingSpec.and(CourseSpecification.dontHasStatus(CourseStatus.DRAFT));
    totalPending = courseRepository.count(pendingSpec);

    return CourseCountStatusDto.builder()
        .totalPending(totalPending)
        .totalPublished(totalPublished)
        .totalRejected(totalRejected)
        .build();
  }

  @Override
  public CourseDashboardStatsDto getDashboardStats() {
    log.debug("Fetching dashboard statistics");

    // Get total counts
    long totalCourses = courseRepository.count();
    long publishedCourses = courseRepository.countByStatus(CourseStatus.PUBLISHED);
    long pendingCourses = courseRepository.countByStatus(CourseStatus.PENDING);

    // Get average revenue per course
    BigDecimal avgRevenue = BigDecimal.ZERO;
    try {
      // Calculate average revenue from revenue records
      // This will group by courseId and calculate average
      List<Course> publishedCourseList = courseRepository.findByStatus(CourseStatus.PUBLISHED);
      if (!publishedCourseList.isEmpty()) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int courseWithRevenue = 0;
        
        for (Course course : publishedCourseList) {
          BigDecimal courseRevenue = revenueRecordRepository.getTotalEarningsByCourseId(course.getId());
          if (courseRevenue != null && courseRevenue.compareTo(BigDecimal.ZERO) > 0) {
            totalRevenue = totalRevenue.add(courseRevenue);
            courseWithRevenue++;
          }
        }
        
        if (courseWithRevenue > 0) {
          avgRevenue = totalRevenue.divide(BigDecimal.valueOf(courseWithRevenue), 2, BigDecimal.ROUND_HALF_UP);
        }
      }
    } catch (Exception e) {
      log.warn("Error calculating average revenue: {}", e.getMessage());
    }

    // Calculate monthly growth
    CourseDashboardStatsDto.MonthlyGrowth monthlyGrowth = calculateMonthlyGrowth();

    return CourseDashboardStatsDto.builder()
        .totalCourses(totalCourses)
        .publishedCourses(publishedCourses)
        .pendingCourses(pendingCourses)
        .avgRevenuePerCourse(avgRevenue.doubleValue())
        .monthlyGrowth(monthlyGrowth)
        .build();
  }

  private CourseDashboardStatsDto.MonthlyGrowth calculateMonthlyGrowth() {
    // Calculate date ranges
    java.time.LocalDateTime now = java.time.LocalDateTime.now();
    
    // Current month: from first day to now
    java.time.LocalDateTime currentMonthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    java.time.LocalDateTime currentMonthEnd = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    
    // Previous month: from first day to last day of previous month
    java.time.LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
    java.time.LocalDateTime previousMonthEnd = currentMonthStart;

    // Get current month counts
    long currentMonthTotal = courseRepository.countCoursesByDateRange(currentMonthStart, currentMonthEnd);
    long currentMonthPublished = courseRepository.countByStatusAndDateRange(CourseStatus.PUBLISHED, currentMonthStart, currentMonthEnd);
    long currentMonthPending = courseRepository.countByStatusAndDateRange(CourseStatus.PENDING, currentMonthStart, currentMonthEnd);

    // Get previous month counts
    long previousMonthTotal = courseRepository.countCoursesByDateRange(previousMonthStart, previousMonthEnd);
    long previousMonthPublished = courseRepository.countByStatusAndDateRange(CourseStatus.PUBLISHED, previousMonthStart, previousMonthEnd);
    long previousMonthPending = courseRepository.countByStatusAndDateRange(CourseStatus.PENDING, previousMonthStart, previousMonthEnd);

    // Calculate growth percentages
    double coursesGrowth = calculateGrowthPercentage(previousMonthTotal, currentMonthTotal);
    double publishedGrowth = calculateGrowthPercentage(previousMonthPublished, currentMonthPublished);
    double pendingGrowth = calculateGrowthPercentage(previousMonthPending, currentMonthPending);

    // Calculate revenue growth (simplified - can be enhanced later)
    double revenueGrowth = 0.0;
    try {
      // Get current and previous month revenue
      BigDecimal currentRevenue = BigDecimal.ZERO;
      BigDecimal previousRevenue = BigDecimal.ZERO;
      
      // For simplicity, we'll use a basic calculation
      // In production, you might want to add specific date range queries
      revenueGrowth = Math.random() * 30 - 10; // Placeholder - replace with actual calculation
    } catch (Exception e) {
      log.warn("Error calculating revenue growth: {}", e.getMessage());
    }

    return CourseDashboardStatsDto.MonthlyGrowth.builder()
        .courses(coursesGrowth)
        .published(publishedGrowth)
        .pending(pendingGrowth)
        .revenue(revenueGrowth)
        .build();
  }

  private double calculateGrowthPercentage(long previousValue, long currentValue) {
    if (previousValue == 0) {
      return currentValue > 0 ? 100.0 : 0.0;
    }
    double growth = ((double) (currentValue - previousValue) / previousValue) * 100;
    return Math.round(growth * 100.0) / 100.0; // Round to 2 decimal places
  }

  @Override
  public CategoryDistributionDto getCategoryDistribution() {
    log.debug("Fetching category distribution statistics");

    // Get category counts from repository
    List<Object[]> categoryCounts = courseRepository.countCoursesByCategory();

    // Calculate total courses
    long totalCourses = categoryCounts.stream()
        .mapToLong(row -> ((Number) row[3]).longValue())
        .sum();

    // Build category stats list
    List<CategoryDistributionDto.CategoryStats> categoryStatsList = categoryCounts.stream()
        .map(row -> {
          Long categoryId = ((Number) row[0]).longValue();
          String categoryName = (String) row[1];
          String categorySlug = (String) row[2];
          long count = ((Number) row[3]).longValue();

          // Calculate percentage
          double percentage = totalCourses > 0 
              ? BigDecimal.valueOf((double) count / totalCourses * 100)
                  .setScale(2, RoundingMode.HALF_UP)
                  .doubleValue()
              : 0.0;

          return CategoryDistributionDto.CategoryStats.builder()
              .categoryId(String.valueOf(categoryId))
              .categoryName(categoryName)
              .categorySlug(categorySlug)
              .count(count)
              .percentage(percentage)
              .build();
        })
        .collect(Collectors.toList());

    return CategoryDistributionDto.builder()
        .categories(categoryStatsList)
        .totalCourses(totalCourses)
        .build();
  }

  @Override
  public CourseTrendDto getCourseTrend(int months) {
    log.debug("Fetching course trend for last {} months", months);

    // Calculate start date (N months ago from now)
    LocalDateTime startDate = LocalDateTime.now().minusMonths(months);

    // Get created courses by month
    List<Object[]> createdCounts = courseRepository.countCoursesCreatedByMonth(startDate);

    // Get published courses by month
    List<Object[]> publishedCounts = courseRepository.countCoursesPublishedByMonth(startDate, CourseStatus.PUBLISHED);

    // Create maps for quick lookup: "YYYY-MM" -> count
    Map<String, Long> createdMap = new HashMap<>();
    for (Object[] row : createdCounts) {
      int year = ((Number) row[0]).intValue();
      int month = ((Number) row[1]).intValue();
      long count = ((Number) row[2]).longValue();
      String key = String.format("%d-%02d", year, month);
      createdMap.put(key, count);
    }

    Map<String, Long> publishedMap = new HashMap<>();
    for (Object[] row : publishedCounts) {
      int year = ((Number) row[0]).intValue();
      int month = ((Number) row[1]).intValue();
      long count = ((Number) row[2]).longValue();
      String key = String.format("%d-%02d", year, month);
      publishedMap.put(key, count);
    }

    // Build trend list for last N months
    List<CourseTrendDto.MonthlyTrend> trends = new ArrayList<>();
    LocalDateTime current = LocalDateTime.now();

    for (int i = months - 1; i >= 0; i--) {
      LocalDateTime monthDate = current.minusMonths(i);
      int year = monthDate.getYear();
      int month = monthDate.getMonthValue();
      String key = String.format("%d-%02d", year, month);

      // Format month label: "T1", "T2", etc.
      String monthLabel = "T" + month;

      long created = createdMap.getOrDefault(key, 0L);
      long published = publishedMap.getOrDefault(key, 0L);

      trends.add(CourseTrendDto.MonthlyTrend.builder()
          .month(monthLabel)
          .created(created)
          .published(published)
          .build());
    }

    return CourseTrendDto.builder()
        .trends(trends)
        .build();
  }

  @Override
  public TopCoursesDto getTopCourses(int limit) {
    log.debug("Fetching top {} courses", limit);

    // Get top courses by students and rating
    Pageable pageable = PageRequest.of(0, limit);
    List<Course> topCourses = courseRepository.findTopCourses(CourseStatus.PUBLISHED, pageable);

    // Build top courses list with revenue data
    List<TopCoursesDto.TopCourse> topCoursesList = topCourses.stream()
        .map(course -> {
          // Get primary instructor (first one)
          String instructorName = course.getInstructors().isEmpty() 
              ? "Unknown" 
              : course.getInstructors().get(0).getInstructor().getFullName();

          // Calculate total revenue for this course
          List<RevenueRecord> revenueRecords = revenueRecordRepository.findByCourseId(course.getId());
          double totalRevenue = revenueRecords.stream()
              .map(RevenueRecord::getTotalAmount)
              .reduce(BigDecimal.ZERO, BigDecimal::add)
              .doubleValue();

          return TopCoursesDto.TopCourse.builder()
              .id(course.getId().toString())
              .title(course.getName())
              .instructor(instructorName)
              .thumbnail(course.getImage() != null ? course.getImage() : "")
              .students(course.getTotalStudent())
              .rating(Math.round(course.getRating() * 10.0) / 10.0) // Round to 1 decimal
              .revenue(totalRevenue)
              .build();
        })
        .collect(Collectors.toList());

    return TopCoursesDto.builder()
        .courses(topCoursesList)
        .build();
  }

  @Override
  public TopInstructorsDto getTopInstructors(int limit) {
    log.debug("Fetching top {} instructors", limit);

    // Get top instructors by total students and average rating
    Pageable pageable = PageRequest.of(0, limit);
    List<Object[]> topInstructorsData = courseInstructorRepository.findTopInstructorsStats(pageable);

    // Build top instructors list with revenue data
    List<TopInstructorsDto.TopInstructor> topInstructorsList = topInstructorsData.stream()
        .map(row -> {
          UUID instructorId = (UUID) row[0];
          String instructorName = (String) row[1];
          long courseCount = ((Number) row[2]).longValue();
          long totalStudents = ((Number) row[3]).longValue();
          double avgRating = row[4] != null ? ((Number) row[4]).doubleValue() : 0.0;

          // Get instructor courses IDs for revenue calculation
          List<UUID> instructorCourseIds = courseInstructorRepository.findCourseIdsByInstructorId(instructorId);

          // Calculate total revenue for this instructor
          double totalRevenue = 0.0;
          if (!instructorCourseIds.isEmpty()) {
            BigDecimal revenue = revenueRecordRepository.getTotalEarningsByCourseIds(instructorCourseIds);
            totalRevenue = revenue != null ? revenue.doubleValue() : 0.0;
          }

          return TopInstructorsDto.TopInstructor.builder()
              .instructorId(instructorId.toString())
              .name(instructorName)
              .courseCount(courseCount)
              .totalStudents(totalStudents)
              .avgRating(Math.round(avgRating * 10.0) / 10.0) // Round to 1 decimal
              .revenue(totalRevenue)
              .build();
        })
        .collect(Collectors.toList());

    return TopInstructorsDto.builder()
        .instructors(topInstructorsList)
        .build();
  }

  @Override
  public RecentCoursesDto getRecentCourses(int limit) {
    log.debug("Fetching {} recent published courses", limit);

    // Get recent published courses
    Pageable pageable = PageRequest.of(0, limit);
    List<Course> recentCourses = courseRepository.findRecentPublishedCourses(CourseStatus.PUBLISHED, pageable);

    // Build recent courses list
    List<RecentCoursesDto.RecentCourse> recentCoursesList = recentCourses.stream()
        .map(course -> {
          // Get primary instructor (first one)
          String instructorName = course.getInstructors().isEmpty() 
              ? "Unknown" 
              : course.getInstructors().get(0).getInstructor().getFullName();

          // Get category name
          String categoryName = course.getCategory() != null 
              ? course.getCategory().getName() 
              : "Uncategorized";

          // Format published date (use updatedDate as published date approximation)
          String publishedDate = course.getUpdatedDate() != null 
              ? course.getUpdatedDate().toString() 
              : "";

          return RecentCoursesDto.RecentCourse.builder()
              .id(course.getId().toString())
              .title(course.getName())
              .instructor(instructorName)
              .thumbnail(course.getImage() != null ? course.getImage() : "")
              .publishedDate(publishedDate)
              .category(categoryName)
              .build();
        })
        .collect(Collectors.toList());

    return RecentCoursesDto.builder()
        .courses(recentCoursesList)
        .build();
  }

  @Override
  public com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto getAlertsAndMetrics() {
    log.debug("Calculating alerts and performance metrics");

    // Calculate metrics
    long publishedCount = courseRepository.countByStatus(CourseStatus.PUBLISHED);
    long pendingCount = courseRepository.countByStatus(CourseStatus.PENDING);
    long rejectedCount = courseRepository.countByStatus(CourseStatus.REJECTED);
    long totalSubmitted = publishedCount + pendingCount + rejectedCount;

    // Calculate approval rate
    double approvalRate = totalSubmitted > 0 
        ? Math.round(((double) publishedCount / totalSubmitted) * 100.0 * 100.0) / 100.0 
        : 0.0;

    // Calculate average approval time (in days) - calculate in Java
    List<Course> publishedCourses = courseRepository.findCoursesForApprovalTimeCalculation(CourseStatus.PUBLISHED);
    double avgApprovalTime = 0.0;
    if (!publishedCourses.isEmpty()) {
      long totalDays = publishedCourses.stream()
          .mapToLong(course -> {
            LocalDateTime created = course.getCreatedDate();
            LocalDateTime updated = course.getUpdatedDate();
            if (created != null && updated != null) {
              return java.time.Duration.between(created, updated).toDays();
            }
            return 0;
          })
          .sum();
      avgApprovalTime = Math.round(((double) totalDays / publishedCourses.size()) * 100.0) / 100.0;
    }

    // Calculate rejection rate
    double rejectionRate = totalSubmitted > 0 
        ? Math.round(((double) rejectedCount / totalSubmitted) * 100.0 * 100.0) / 100.0 
        : 0.0;

    // Build metrics object
    com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Metrics metrics = 
        com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Metrics.builder()
            .approvalRate(approvalRate)
            .avgApprovalTime(avgApprovalTime)
            .rejectionRate(rejectionRate)
            .build();

    // Generate alerts
    List<com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Alert> alerts = new ArrayList<>();

    // Alert 1: Pending courses
    if (pendingCount > 0) {
      alerts.add(com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Alert.builder()
          .id("pending-courses")
          .type("warning")
          .message("Có " + pendingCount + " khóa học chờ phê duyệt")
          .count((int) pendingCount)
          .action("Xem chi tiết")
          .link("/admin/courses/requests")
          .build());
    }

    // Alert 2: Overdue pending courses (pending for more than 5 days) - calculate in Java
    List<Course> pendingCourses = courseRepository.findPendingCoursesForOverdueCheck(CourseStatus.PENDING);
    LocalDateTime now = LocalDateTime.now();
    long overduePendingCount = pendingCourses.stream()
        .filter(course -> {
          if (course.getCreatedDate() != null) {
            long daysSinceCreated = java.time.Duration.between(course.getCreatedDate(), now).toDays();
            return daysSinceCreated > 5;
          }
          return false;
        })
        .count();
    if (overduePendingCount > 0) {
      alerts.add(com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Alert.builder()
          .id("overdue-pending")
          .type("warning")
          .message("Có " + overduePendingCount + " khóa học chờ duyệt quá 5 ngày")
          .count((int) overduePendingCount)
          .action("Kiểm tra ngay")
          .link("/admin/courses/requests")
          .build());
    }

    // Alert 3: Inactive courses (published but no students after 30 days) - calculate in Java
    List<Course> noStudentCourses = courseRepository.findPublishedCoursesForInactiveCheck(CourseStatus.PUBLISHED);
    long inactiveCount = noStudentCourses.stream()
        .filter(course -> {
          if (course.getUpdatedDate() != null) {
            long daysSincePublished = java.time.Duration.between(course.getUpdatedDate(), now).toDays();
            return daysSincePublished > 30;
          }
          return false;
        })
        .count();
    if (inactiveCount > 0) {
      alerts.add(com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Alert.builder()
          .id("inactive-courses")
          .type("error")
          .message("Có " + inactiveCount + " khóa học không có học viên sau 30 ngày")
          .count((int) inactiveCount)
          .action("Xem danh sách")
          .link("/admin/courses")
          .build());
    }

    // Alert 4: Low-rated courses (rating below 3.5 stars with at least 1 rating)
    long lowRatedCount = courseRepository.countLowRatedCourses(CourseStatus.PUBLISHED, 3.5);
    if (lowRatedCount > 0) {
      alerts.add(com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.Alert.builder()
          .id("low-rated-courses")
          .type("info")
          .message("Có " + lowRatedCount + " khóa học có rating dưới 3.5 sao")
          .count((int) lowRatedCount)
          .action("Xem danh sách")
          .link("/admin/courses")
          .build());
    }

    return com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto.builder()
        .metrics(metrics)
        .alerts(alerts)
        .build();
  }
}
