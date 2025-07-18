package com.vinaacademy.platform.feature.instructor.service;

import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDto;
import com.vinaacademy.platform.feature.instructor.dto.CourseInstructorDtoRequest;

public interface CourseInstructorService {
    CourseInstructorDto createCourseInstructor(CourseInstructorDtoRequest dto);
}
