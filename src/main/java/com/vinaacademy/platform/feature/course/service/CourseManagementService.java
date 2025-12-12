package com.vinaacademy.platform.feature.course.service;

import java.util.UUID;

public interface CourseManagementService {
    Boolean isInstructorOfCourse(UUID courseId, UUID instructorId);
}
