//package com.vinaacademy.platform.feature.course.service;
//
//import com.vinaacademy.platform.feature.course.dto.CourseCountStatusDto;
//import com.vinaacademy.platform.feature.course.dto.CourseDetailsResponse;
//import com.vinaacademy.platform.feature.course.dto.CourseDto;
//import com.vinaacademy.platform.feature.course.dto.CourseRequest;
//import com.vinaacademy.platform.feature.course.dto.CourseSearchRequest;
//import com.vinaacademy.platform.feature.course.dto.CourseStatusRequest;
//import com.vinaacademy.platform.feature.course.permission.CoursePermissionService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.UUID;
//
///**
// * Main course service implementation that delegates to command and query services.
// * This implementation provides backward compatibility during the refactoring process.
// *
// * @deprecated Use CourseCommandService and CourseQueryService directly for new code.
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@Deprecated(since = "2.0", forRemoval = true)
//public class CourseServiceImpl implements CourseService {
//
//    private final CourseCommandService courseCommandService;
//    private final CourseQueryService courseQueryService;
//    private final CoursePermissionService coursePermissionService;
//
//    @Override
//    public Boolean isInstructorOfCourse(UUID courseId, UUID instructorId) {
//        return coursePermissionService.isInstructorOfCourse(courseId, instructorId);
//    }
//
//    @Override
//    public List<CourseDto> getCourses() {
//        return courseQueryService.getCourses();
//    }
//
//    @Override
//    public Boolean existByCourseSlug(String slug) {
//        return courseQueryService.existByCourseSlug(slug);
//    }
//
//    @Override
//    public CourseDetailsResponse getCourse(String slug) {
//        return courseQueryService.getCourse(slug);
//    }
//
//    @Override
//    public Page<CourseDetailsResponse> searchCourseDetails(CourseSearchRequest searchRequest, Pageable pageable) {
//        return courseQueryService.searchCourseDetails(searchRequest, pageable);
//    }
//
//    @Override
//    public CourseDto createCourse(CourseRequest request) {
//        return courseCommandService.createCourse(request);
//    }
//
//    @Override
//    public CourseDto updateCourse(String slug, CourseRequest request) {
//        return courseCommandService.updateCourse(slug, request);
//    }
//
//    @Override
//    public void deleteCourse(String slug) {
//        courseCommandService.deleteCourse(slug);
//    }
//
//    @Override
//    public List<CourseDto> getCoursesByCategory(String slug) {
//        return courseQueryService.getCoursesByCategory(slug);
//    }
//
//    @Override
//    public Page<CourseDto> getCoursesPaginated(String categorySlug, double minRating, Pageable pageable) {
//        return courseQueryService.getCoursesPaginated(categorySlug, minRating, pageable);
//    }
//
//    @Override
//    public Page<CourseDto> searchCourses(CourseSearchRequest searchRequest, Pageable pageable) {
//        return courseQueryService.searchPublishedCourses(searchRequest, pageable);
//    }
//
//    @Override
//    public Page<CourseDto> getCoursesByInstructor(UUID instructorId, Pageable pageable) {
//        return courseQueryService.getCoursesByInstructor(instructorId, pageable);
//    }
//
//    @Override
//    public Page<CourseDto> searchInstructorCourses(UUID instructorId, CourseSearchRequest searchRequest, Pageable pageable) {
//        return courseQueryService.searchInstructorCourses(instructorId, searchRequest, pageable);
//    }
//
//    @Override
//    public CourseDto getCourseLearning(String slug) {
//        return courseQueryService.getCourseLearning(slug);
//    }
//
//    @Override
//    public CourseDto getCourseById(UUID id) {
//        return courseQueryService.getCourseById(id);
//    }
//
//    @Override
//    public String getCourseSlugById(UUID id) {
//        return courseQueryService.getCourseSlugById(id);
//    }
//
//    @Override
//    public Boolean updateStatusCourse(CourseStatusRequest courseStatusRequest) {
//        return courseCommandService.updateStatusCourse(courseStatusRequest);
//    }
//
//    @Override
//    public CourseCountStatusDto getCountCourses() {
//        return courseQueryService.getCountCourses();
//    }
//
//    @Override
//    public Page<CourseDto> getPublishedCoursesByInstructor(UUID instructorId, Pageable pageable) {
//        return courseQueryService.getPublishedCoursesByInstructor(instructorId, pageable);
//    }
//}
