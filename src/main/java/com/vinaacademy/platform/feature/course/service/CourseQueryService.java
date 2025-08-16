package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for course query operations.
 * Handles all read-only operations for courses.
 */
public interface CourseQueryService {

    /**
     * Get course details by slug
     *
     * @param slug The course slug
     * @return Course details response
     */
    CourseDetailsResponse getCourseBySlug(String slug);

    /**
     * Get course details by ID
     *
     * @param id The course ID
     * @return Course details response
     */
    CourseDetailsResponse getCourseById(UUID id);

    /**
     * Get course basic info by ID
     *
     * @param id The course ID
     * @return Course DTO
     */
    CourseDto getCourseInfoById(UUID id);

    /**
     * Get course for learning (with progress and enrollment check)
     *
     * @param slug The course slug
     * @return Course DTO with learning information
     */
    CourseDto getCourseLearning(String slug);

    /**
     * Get course for learning by ID (with progress and enrollment check)
     *
     * @param id The course ID
     * @return Course DTO with learning information
     */
    CourseDto getCourseLearningById(UUID id);

    /**
     * Search courses with advanced criteria (public search)
     *
     * @param searchRequest Search criteria
     * @param pageable      Pagination information
     * @return Page of matching courses
     */
    Page<CourseDto> searchPublishedCourses(CourseSearchRequest searchRequest, Pageable pageable);

    /**
     * Search course details (admin search)
     *
     * @param searchRequest Search criteria
     * @param pageable      Pagination information
     * @return Page of matching course details
     */
    Page<CourseDetailsResponse> searchCourseDetails(CourseSearchRequest searchRequest, Pageable pageable);

    /**
     * Search instructor's courses
     *
     * @param instructorId  The instructor ID
     * @param searchRequest Search criteria
     * @param pageable      Pagination information
     * @return Page of matching instructor courses
     */
    Page<CourseDto> searchInstructorCourses(UUID instructorId, CourseSearchRequest searchRequest, Pageable pageable);


    /**
     * Get course count by status
     *
     * @return Course count statistics
     */
    CourseCountStatusDto getCountCourses();
}
