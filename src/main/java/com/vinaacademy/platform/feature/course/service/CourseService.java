package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
import com.vinaacademy.platform.feature.course.dto.CourseDto;
import com.vinaacademy.platform.feature.course.dto.CourseRequest;
import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
import com.vinaacademy.platform.feature.course.dto.CourseStatusRequest;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CourseService {

        Boolean isInstructorOfCourse(UUID courseId, UUID instructorId);

        List<CourseDto> getCourses();

        List<CourseDto> getCoursesByCategory(String slug);

        Page<CourseDto> getCoursesPaginated(
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection,
                        String categorySlug,
                        double minRating);

        Page<CourseDto> searchCourses(
                        CourseSearchRequest searchRequest,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection);

        Page<CourseDto> getCoursesByInstructor(
                        UUID instructorId,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection);

        Page<CourseDto> searchInstructorCourses(
                        UUID instructorId,
                        CourseSearchRequest searchRequest,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection);

        Page<CourseDto> getPublishedCoursesByInstructor(
                        UUID instructorId,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection);

        CourseDetailsResponse getCourse(String slug);

        CourseDto createCourse(CourseRequest request);

        CourseDto updateCourse(String slug, CourseRequest request);

        void deleteCourse(String slug);

        CourseDto getCourseLearning(String slug);

        CourseDto getCourseById(UUID id);

        String getCourseSlugById(UUID id);

        Boolean existByCourseSlug(String slug);

        Boolean updateStatusCourse(CourseStatusRequest courseStatusRequest);

        Page<CourseDetailsResponse> searchCourseDetails(CourseSearchRequest searchRequest, int page, int size,
                        String sortBy, String sortDirection);

        CourseCountStatusDto getCountCourses();
}
