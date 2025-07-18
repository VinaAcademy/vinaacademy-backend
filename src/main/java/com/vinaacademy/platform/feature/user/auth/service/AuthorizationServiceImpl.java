package com.vinaacademy.platform.feature.user.auth.service;

import com.vinaacademy.platform.exception.UnauthorizedException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.repository.projection.LessonAccessInfoDto;
import com.vinaacademy.platform.feature.user.auth.helpers.AccessHelper;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    private final SecurityHelper securityHelper;
    private final AccessHelper accessHelper;

    private final LessonRepository lessonRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public boolean hasRole(String role) {
        return securityHelper.hasRole(role);
    }

    @Override
    public boolean hasAnyRole(String... roles) {
        return securityHelper.hasAnyRole(roles);
    }

    @Override
    @Transactional
    public boolean canModifyResource(UUID resourceAuthorId) {
        try {
            User currentUser = securityHelper.getCurrentUser();
            return currentUser.getId().equals(resourceAuthorId) || hasRole(AuthConstants.ADMIN_ROLE);
        } catch (UnauthorizedException e) {
            log.warn("Unauthorized user attempted to modify resource: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessLesson(UUID lessonId) {
        User currentUser = securityHelper.getCurrentUser();
        return canAccessLesson(lessonId, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessLesson(UUID lessonId, User user) {
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        Optional<LessonAccessInfoDto> lessonAccessInfo = lessonRepository.getLessonAccessInfo(lessonId, user.getId());
        return lessonAccessInfo.filter(lessonAccessInfoDto -> lessonAccessInfoDto.isFree() ||
                lessonAccessInfoDto.isInstructor() ||
                lessonAccessInfoDto.isEnrolled()).isPresent();
    }

    @Override
    public boolean canModifyLesson(UUID lessonId) {
        User user = securityHelper.getCurrentUser();
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        Optional<LessonAccessInfoDto> lessonAccessInfo = lessonRepository.getLessonAccessInfo(lessonId, user.getId());
        return lessonAccessInfo.filter(LessonAccessInfoDto::isInstructor)
                .isPresent();
    }

    @Override
    public boolean canModifyCourse(UUID courseId) {
        User user = securityHelper.getCurrentUser();
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        return courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, user.getId());
    }

    @Override
    public boolean canAccessCourse(UUID courseId) {
        User user = securityHelper.getCurrentUser();
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        boolean isEnrolled = enrollmentRepository.existsByCourseIdAndUserId(courseId, user.getId());
        boolean isInstructor = courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, user.getId());

        return isEnrolled || isInstructor;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessSection(UUID sectionId) {
        User user = securityHelper.getCurrentUser();
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        Course course = courseRepository.getCourseBySectionId(sectionId)
                .orElseThrow(() -> new UnauthorizedException("Course not found for section ID: " + sectionId));

        boolean isEnrolled = enrollmentRepository.existsByCourseIdAndUserId(course.getId(), user.getId());
        boolean isInstructor = courseInstructorRepository.existsByCourseIdAndInstructorId(course.getId(), user.getId());

        return isEnrolled || isInstructor;
    }

    @Override
    public boolean canModifySection(UUID sectionId) {
        User user = securityHelper.getCurrentUser();
        // Admins always have access
        if (accessHelper.isAdmin(user) || accessHelper.isStaff(user)) {
            return true;
        }

        Course course = courseRepository.getCourseBySectionId(sectionId)
                .orElseThrow(() -> new UnauthorizedException("Course not found for section ID: " + sectionId));

        return courseInstructorRepository.existsByCourseIdAndInstructorId(course.getId(), user.getId());
    }

}