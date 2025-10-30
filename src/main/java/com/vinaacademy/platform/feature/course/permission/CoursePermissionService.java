package com.vinaacademy.platform.feature.course.permission;

import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.user.entity.User;

import java.util.UUID;

/**
 * Service for handling course-specific permission checks.
 * Centralizes ownership and access logic for courses.
 */
public interface CoursePermissionService {

    /**
     * Checks if the user is an instructor/owner of the course
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @return true if the user is an instructor of the course
     */
    boolean isInstructorOfCourse(UUID courseId, UUID userId);

    /**
     * Checks if the user is an instructor/owner of the course
     *
     * @param course The course entity
     * @param user   The user entity
     * @return true if the user is an instructor of the course
     */
    boolean isInstructorOfCourse(Course course, User user);

    /**
     * Checks if the user can modify the course
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @return true if the user can modify the course
     */
    boolean canModifyCourse(UUID courseId, UUID userId);

    /**
     * Checks if the user can modify the course
     *
     * @param course The course entity
     * @param user   The user entity
     * @return true if the user can modify the course
     */
    boolean canModifyCourse(Course course, User user);

    /**
     * Checks if the user can delete the course
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @return true if the user can delete the course
     */
    boolean canDeleteCourse(UUID courseId, UUID userId);

    /**
     * Checks if the user can delete the course
     *
     * @param course The course entity
     * @param user   The user entity
     * @return true if the user can delete the course
     */
    boolean canDeleteCourse(Course course, User user);

    /**
     * Checks if the user can view the course (for learning purposes)
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @return true if the user can view the course
     */
    boolean canAccessCourseForLearning(UUID courseId, UUID userId);

    /**
     * Validates that the user has permission to modify the course, throws exception if not
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @throws com.vinaacademy.platform.exception.BadRequestException if permission denied
     */
    void validateCourseModifyPermission(UUID courseId, UUID userId);

    /**
     * Validates that the user has permission to delete the course, throws exception if not
     *
     * @param courseId The course ID
     * @param userId   The user ID
     * @throws com.vinaacademy.platform.exception.BadRequestException if permission denied
     */
    void validateCourseDeletePermission(UUID courseId, UUID userId);
}
