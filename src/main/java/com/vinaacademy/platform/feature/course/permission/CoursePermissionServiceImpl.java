package com.vinaacademy.platform.feature.course.permission;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.constant.AuthConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of course permission service.
 * Handles all course-related authorization logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoursePermissionServiceImpl implements CoursePermissionService {

    private final CourseRepository courseRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    public boolean isInstructorOfCourse(UUID courseId, UUID userId) {
        log.debug("Checking if user {} is instructor of course {}", userId, courseId);
        return courseInstructorRepository.existsByCourseIdAndInstructorId(courseId, userId);
    }

    @Override
    public boolean isInstructorOfCourse(Course course, User user) {
        if (course == null || user == null) {
            return false;
        }
        return course.getInstructors().stream()
                .anyMatch(courseInstructor -> courseInstructor.getInstructor().equals(user));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canModifyCourse(UUID courseId, UUID userId) {
        log.debug("Checking if user {} can modify course {}", userId, courseId);
        
        // Admin and staff can modify any course
        if (securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)) {
            return true;
        }
        
        // Instructors can only modify their own courses
        if (securityHelper.hasAnyRole(AuthConstants.INSTRUCTOR_ROLE)) {
            return isInstructorOfCourse(courseId, userId);
        }
        
        return false;
    }

    @Override
    public boolean canModifyCourse(Course course, User user) {
        if (course == null || user == null) {
            return false;
        }
        
        // Admin and staff can modify any course
        boolean isAdminOrStaff = user.getRoles().stream()
                .anyMatch(role -> AuthConstants.ADMIN_ROLE.equalsIgnoreCase(role.getCode()) ||
                                AuthConstants.STAFF_ROLE.equalsIgnoreCase(role.getCode()));
        if (isAdminOrStaff) {
            return true;
        }
        
        // Instructors can only modify their own courses
        boolean isInstructor = user.getRoles().stream()
                .anyMatch(role -> AuthConstants.INSTRUCTOR_ROLE.equalsIgnoreCase(role.getCode()));
        if (isInstructor) {
            return isInstructorOfCourse(course, user);
        }
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteCourse(UUID courseId, UUID userId) {
        log.debug("Checking if user {} can delete course {}", userId, courseId);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> BadRequestException.message("course.not_found"));
        
        // Cannot delete if course has students
        if (course.getTotalStudent() > 0) {
            return false;
        }
        
        // Admin and staff can delete any course (without students)
        if (securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)) {
            return true;
        }
        
        // Instructors can only delete their own courses
        if (securityHelper.hasAnyRole(AuthConstants.INSTRUCTOR_ROLE)) {
            return isInstructorOfCourse(courseId, userId);
        }
        
        return false;
    }

    @Override
    public boolean canDeleteCourse(Course course, User user) {
        if (course == null || user == null) {
            return false;
        }
        
        // Cannot delete if course has students
        if (course.getTotalStudent() > 0) {
            return false;
        }
        
        // Admin and staff can delete any course (without students)
        boolean isAdminOrStaff = user.getRoles().stream()
                .anyMatch(role -> AuthConstants.ADMIN_ROLE.equalsIgnoreCase(role.getCode()) ||
                                AuthConstants.STAFF_ROLE.equalsIgnoreCase(role.getCode()));
        if (isAdminOrStaff) {
            return true;
        }
        
        // Instructors can only delete their own courses
        boolean isInstructor = user.getRoles().stream()
                .anyMatch(role -> AuthConstants.INSTRUCTOR_ROLE.equalsIgnoreCase(role.getCode()));
        if (isInstructor) {
            return isInstructorOfCourse(course, user);
        }
        
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canAccessCourseForLearning(UUID courseId, UUID userId) {
        log.debug("Checking if user {} can access course {} for learning", userId, courseId);
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> BadRequestException.message("course.not_found"));
        
        // Admin and staff can access any course
        if (securityHelper.hasAnyRole(AuthConstants.ADMIN_ROLE, AuthConstants.STAFF_ROLE)) {
            return true;
        }
        
        // Instructors can access their own courses
        if (securityHelper.hasAnyRole(AuthConstants.INSTRUCTOR_ROLE) && 
            isInstructorOfCourse(courseId, userId)) {
            return true;
        }
        
        // Students need to be enrolled
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BadRequestException.message("user.not_found"));
        
        return enrollmentRepository.findByCourseAndUser(course, user).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCourseModifyPermission(UUID courseId, UUID userId) {
        if (!canModifyCourse(courseId, userId)) {
            throw BadRequestException.message("course.permission.modify_denied");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateCourseDeletePermission(UUID courseId, UUID userId) {
        if (!canDeleteCourse(courseId, userId)) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> BadRequestException.message("course.not_found"));
            
            if (course.getTotalStudent() > 0) {
                throw BadRequestException.message("course.delete.has_students");
            }
            
            throw BadRequestException.message("course.permission.delete_denied");
        }
    }
}
