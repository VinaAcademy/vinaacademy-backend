package com.vinaacademy.platform.feature.course.service;

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

    /**
     * Get dashboard statistics including counts, revenue, and growth rates
     *
     * @return Dashboard statistics
     */
    CourseDashboardStatsDto getDashboardStats();

    /**
     * Get category distribution statistics
     *
     * @return Category distribution with course counts and percentages
     */
    CategoryDistributionDto getCategoryDistribution();

    /**
     * Get course trend statistics over the last N months
     *
     * @param months Number of months to look back (default 6)
     * @return Monthly trend data for created and published courses
     */
    CourseTrendDto getCourseTrend(int months);

    /**
     * Get top N courses by performance metrics
     *
     * @param limit Maximum number of courses to return (default 10)
     * @return Top courses with ranking
     */
    TopCoursesDto getTopCourses(int limit);

    /**
     * Get top N instructors by performance metrics
     *
     * @param limit Maximum number of instructors to return (default 10)
     * @return Top instructors with aggregated statistics
     */
    TopInstructorsDto getTopInstructors(int limit);

    /**
     * Get recently published courses
     *
     * @param limit Maximum number of courses to return (default 12)
     * @return Recent published courses ordered by publish date
     */
    RecentCoursesDto getRecentCourses(int limit);

    /**
     * Get alerts and performance metrics for dashboard
     *
     * @return Alerts and metrics including approval rate, avg approval time, rejection rate, and dynamic alerts
     */
    com.vinaacademy.platform.feature.course.dto.AlertsAndMetricsDto getAlertsAndMetrics();
}
