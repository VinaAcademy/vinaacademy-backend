package com.vinaacademy.platform.feature.course.service;

import com.vinaacademy.platform.feature.course.permission.CoursePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseManagementServiceImpl implements CourseManagementService {
    private final CoursePermissionService coursePermissionService;
    @Override
    public Boolean isInstructorOfCourse(UUID courseId, UUID instructorId) {
        return coursePermissionService.isInstructorOfCourse(courseId, instructorId);
    }
}
