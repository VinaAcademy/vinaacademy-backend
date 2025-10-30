package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.course.assembler.CourseAssembler;
import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.mapper.CourseMapper;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.course.repository.specification.CourseSpecBuilder;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.dto.EnrollmentProgressDto;
import com.vinaacademy.platform.feature.enrollment.mapper.EnrollmentMapper;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.section.dto.SectionDto;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
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
  private final CourseMapper courseMapper;
  private final SecurityHelper securityHelper;
  private final CourseAssembler courseAssembler;

  @Override
  @Cacheable(value = "courseDetails", key = "#slug", unless = "#result == null")
  public CourseDetailsResponse getCourseBySlug(String slug) {
    log.debug("Fetching course details for slug: {}", slug);

    Course course =
        courseRepository
            .findBySlugWithDetails(slug)
            .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

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
    return coursePage.map(courseMapper::toDTO);
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

    return CourseCountStatusDto.builder()
        .totalPending(totalPending)
        .totalPublished(totalPublished)
        .totalRejected(totalRejected)
        .build();
  }
}
