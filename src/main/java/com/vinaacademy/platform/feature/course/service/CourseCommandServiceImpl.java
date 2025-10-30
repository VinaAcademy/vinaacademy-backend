package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.category.repository.CategoryRepository;
import com.vinaacademy.platform.feature.common.helpers.SlugGeneratorHelper;
import com.vinaacademy.platform.feature.common.utils.SlugUtils;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.event.CourseStatusChangedEvent;
import com.vinaacademy.platform.feature.course.event.CourseSubmittedForReviewEvent;
import com.vinaacademy.platform.feature.course.mapper.CourseMapper;
import com.vinaacademy.platform.feature.course.permission.CoursePermissionService;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of course command service.
 * Handles all write operations for courses.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseCommandServiceImpl implements CourseCommandService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final CourseMapper courseMapper;
    private final SecurityHelper securityHelper;
    private final SlugGeneratorHelper slugGeneratorHelper;
    private final CoursePermissionService coursePermissionService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    @CacheEvict(value = {"coursesByCategory", "courseDetails", "courseExists", "courseById", "courseSlugById"}, allEntries = true)
    public CourseDto createCourse(CourseRequest request) {
        log.debug("Creating course with name: {}", request.getName());

        String baseSlug = StringUtils.isBlank(request.getSlug()) ?
                SlugUtils.toSlug(request.getName()) :
                request.getSlug();
        String slug = slugGeneratorHelper.generateSlug(baseSlug, s -> !courseRepository.existsBySlug(s));

        Category category = categoryRepository.findBySlug(request.getCategorySlug())
                .orElseThrow(() -> BadRequestException.messageKey("category.not_found"));

        Course course = Course.builder()
                .name(request.getName())
                .category(category)
                .description(request.getDescription())
                .image(request.getImage())
                .language(request.getLanguage())
                .level(request.getLevel())
                .price(request.getPrice())
                .rating(0)
                .slug(slug)
                .status(CourseStatus.DRAFT)
                .build();

        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {} and slug: {}", savedCourse.getId(), savedCourse.getSlug());

        return courseMapper.toDTO(savedCourse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"coursesByCategory", "courseDetails", "courseExists", "courseById", "courseSlugById"}, allEntries = true)
    public CourseDto updateCourse(UUID id, CourseRequest request) {
        log.debug("Updating course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

        User currentUser = securityHelper.getCurrentUser();

        // Use permission service for authorization check
        if (!coursePermissionService.canModifyCourse(course, currentUser)) {
            throw BadRequestException.messageKey("course.permission.modify_denied");
        }

        // Handle slug update with improved logic
        String newSlug = generateUpdatedSlug(course, request);

        Category category = categoryRepository.findBySlug(request.getCategorySlug())
                .orElseThrow(() -> BadRequestException.messageKey("category.not_found"));

        // Update course fields
        course.setName(request.getName());
        course.setSlug(newSlug);
        course.setCategory(category);
        course.setDescription(request.getDescription());
        course.setImage(request.getImage());
        course.setLanguage(request.getLanguage());
        course.setLevel(request.getLevel());
        course.setPrice(request.getPrice());

        Course savedCourse = courseRepository.save(course);
        log.info("Course updated successfully with ID: {} and slug: {}", savedCourse.getId(), savedCourse.getSlug());

        return courseMapper.toDTO(savedCourse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"coursesByCategory", "courseDetails", "courseExists", "courseById", "courseSlugById"}, allEntries = true)
    public void deleteCourse(UUID id) {
        log.debug("Deleting course with id: {}", id);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

        User currentUser = securityHelper.getCurrentUser();

        // Business Rules for Course Deletion:
        // 1. ADMIN can delete any course that has no active enrollments
        // 2. STAFF can delete any course that has no active enrollments  
        // 3. INSTRUCTOR can only delete courses they own (isOwner=true) and have no enrollments

        boolean isAdminOrStaff = securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE);

        if (!isAdminOrStaff) {
            // For instructors, validate ownership and no enrollments
            coursePermissionService.validateCourseDeletePermission(course.getId(), currentUser.getId());
        }

        // Additional business rule: Cannot delete courses with active enrollments
        // This applies to all roles for data integrity
        if (hasActiveEnrollments(course)) {
            throw BadRequestException.messageKey("course.has_active_enrollments");
        }

        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {} and slug: {} by user: {}",
                course.getId(), course.getSlug(), currentUser.getId());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"coursesByCategory", "courseDetails", "courseExists", "courseById", "courseSlugById"}, allEntries = true)
    public Boolean updateStatusCourse(UUID id, CourseStatus status) {
        log.debug("Updating status for course with id: {} to status: {}", id, status);

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

        if (status == null) {
            throw BadRequestException.messageKey("course.status.required");
        }

        if (course.getInstructors().isEmpty()) {
            throw BadRequestException.messageKey("course.no_instructors");
        }

        CourseStatus previousStatus = course.getStatus();
        course.setStatus(status);
        courseRepository.save(course);

        // Publish domain event for status change
        publishCourseStatusChangedEvent(course, previousStatus, status);

        log.info("Course status updated successfully for course ID: {} to status: {}",
                course.getId(), status);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"coursesByCategory", "courseDetails", "courseExists", "courseById", "courseSlugById"}, allEntries = true)
    public Boolean submitCourseForReview(UUID courseId) {
        log.debug("Submitting course for review with ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> BadRequestException.messageKey("course.not_found"));

        User currentUser = securityHelper.getCurrentUser();

        // Validate that current user is instructor of the course
        if (!coursePermissionService.isInstructorOfCourse(course, currentUser)) {
            throw BadRequestException.messageKey("course.permission.modify_denied");
        }

        CourseStatus previousStatus = course.getStatus();
        course.setStatus(CourseStatus.PENDING);
        courseRepository.save(course);

        // Publish domain event for course submission
        publishCourseSubmittedForReviewEvent(course, currentUser);

        // Also publish status change event
        publishCourseStatusChangedEvent(course, previousStatus, CourseStatus.PENDING);

        log.info("Course submitted for review successfully with ID: {} by user: {}", courseId, currentUser.getId());

        return true;
    }

    /**
     * Generate updated slug for course with improved logic
     * - If user provides slug in request, use it (don't auto-generate from name)
     * - If no slug provided, generate from name
     * - Ensure uniqueness in both cases
     */
    private String generateUpdatedSlug(Course course, CourseRequest request) {
        String oldSlug = course.getSlug();
        String newSlug;

        if (StringUtils.isNotBlank(request.getSlug())) {
            // User explicitly provided a slug, use it as-is (don't generate from name)
            newSlug = request.getSlug().trim();
        } else {
            // No slug provided, generate from name
            newSlug = SlugUtils.toSlug(request.getName());
        }

        // If slug hasn't changed, keep the old one
        if (oldSlug.equals(newSlug)) {
            return oldSlug;
        }

        // Check if new slug is unique (excluding current course)
        if (courseRepository.existsBySlugAndIdNot(newSlug, course.getId())) {
            // Generate unique slug using helper
            newSlug = slugGeneratorHelper.generateSlug(newSlug, s -> !courseRepository.existsBySlugAndIdNot(s, course.getId()));
        }

        return newSlug;
    }

    /**
     * Publish course status changed event
     */
    private void publishCourseStatusChangedEvent(Course course, CourseStatus previousStatus, CourseStatus newStatus) {
        try {
            User currentUser = securityHelper.getCurrentUser();
            UUID owner = course.getInstructors().stream()
                    .filter(CourseInstructor::getIsOwner)
                    .findFirst()
                    .map(ci -> ci.getInstructor().getId())
                    .orElse(course.getInstructors().get(0).getInstructor().getId());

            CourseStatusChangedEvent event = CourseStatusChangedEvent.builder()
                    .courseId(course.getId())
                    .courseSlug(course.getSlug())
                    .courseName(course.getName())
                    .previousStatus(previousStatus)
                    .newStatus(newStatus)
                    .actorId(currentUser.getId())
                    .timestamp(LocalDateTime.now())
                    .owner(owner)
                    .build();

            eventPublisher.publishEvent(event);
            log.debug("Published course status changed event for course: {}", course.getId());
        } catch (Exception e) {
            log.error("Failed to publish course status changed event for course: {}", course.getId(), e);
            // Don't rethrow as event publishing failure should not break the main operation
        }
    }

    /**
     * Publish course submitted for review event
     */
    private void publishCourseSubmittedForReviewEvent(Course course, User instructor) {
        try {
            CourseSubmittedForReviewEvent event = CourseSubmittedForReviewEvent.builder()
                    .courseId(course.getId())
                    .courseSlug(course.getSlug())
                    .courseName(course.getName())
                    .instructorId(instructor.getId())
                    .timestamp(LocalDateTime.now())
                    .build();

            eventPublisher.publishEvent(event);
            log.debug("Published course submitted for review event for course: {}", course.getId());
        } catch (Exception e) {
            log.error("Failed to publish course submitted for review event for course: {}", course.getId(), e);
            // Don't rethrow as event publishing failure should not break the main operation
        }
    }

    /**
     * Check if course has active enrollments
     * Used to prevent deletion of courses with students
     */
    private boolean hasActiveEnrollments(Course course) {
        // A course has active enrollments if it has any enrollments
        // This is a business rule to protect data integrity
        return course.getEnrollments() != null && !course.getEnrollments().isEmpty();
    }
}
