package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;

import java.util.UUID;

/**
 * Service interface for course command operations.
 * Handles all write operations for courses.
 */
public interface CourseCommandService {

    /**
     * Create a new course
     *
     * @param request The course creation request
     * @return Created course DTO
     */
    CourseDto createCourse(CourseRequest request);

    /**
     * Update an existing course
     *
     * @param id      The course ID
     * @param request The course update request
     * @return Updated course DTO
     */
    CourseDto updateCourse(UUID id, CourseRequest request);

    /**
     * Delete a course
     *
     * @param id The course ID
     */
    void deleteCourse(UUID id);

    /**
     * Update course status
     *
     * @param id     The course ID
     * @param status The new course status
     * @return true if update successful
     */
    Boolean updateStatusCourse(UUID id, CourseStatus status);

    /**
     * Submit course for review (change status to PENDING)
     *
     * @param courseId The course ID
     * @return true if submission successful
     */
    Boolean submitCourseForReview(UUID courseId);
}
