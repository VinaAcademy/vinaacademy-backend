package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.dto.CourseStatusRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Main course service interface that delegates to command and query services.
 * This interface is kept for backward compatibility during the refactoring process.
 * 
 * @deprecated Use CourseCommandService and CourseQueryService directly for new code.
 */
@Deprecated(since = "2.0", forRemoval = true)
public interface CourseService {

        Boolean isInstructorOfCourse(UUID courseId, UUID instructorId);

        List<CourseDto> getCourses();

        List<CourseDto> getCoursesByCategory(String slug);

        Page<CourseDto> getCoursesPaginated(
                        String categorySlug,
                        double minRating,
                        Pageable pageable);

        Page<CourseDto> searchCourses(
                        CourseSearchRequest searchRequest,
                        Pageable pageable);

        Page<CourseDto> getCoursesByInstructor(
                        UUID instructorId,
                        Pageable pageable);

        Page<CourseDto> searchInstructorCourses(
                        UUID instructorId,
                        CourseSearchRequest searchRequest,
                        Pageable pageable);

        Page<CourseDto> getPublishedCoursesByInstructor(
                        UUID instructorId,
                        Pageable pageable);

        CourseDetailsResponse getCourse(String slug);

        CourseDto createCourse(CourseRequest request);

        CourseDto updateCourse(String slug, CourseRequest request);

        void deleteCourse(String slug);

        CourseDto getCourseLearning(String slug);

        CourseDto getCourseById(UUID id);

        String getCourseSlugById(UUID id);

        Boolean existByCourseSlug(String slug);

        Boolean updateStatusCourse(CourseStatusRequest courseStatusRequest);

        Page<CourseDetailsResponse> searchCourseDetails(CourseSearchRequest searchRequest, Pageable pageable);

        CourseCountStatusDto getCountCourses();
}
