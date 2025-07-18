package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.instructor.repository.CourseInstructorRepository;
import com.vinaacademy.platform.feature.lesson.dto.UserProgressDto;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.lesson.mapper.UserProgressMapper;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.repository.CourseUserProgressRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProgressServiceImpl implements UserProgressService {

    private final CourseUserProgressRepository userProgressRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseInstructorRepository courseInstructorRepository;
    private final UserProgressMapper userProgressMapper;
    private final SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    public List<UserProgressDto> getProgressByUser(UUID userId, UUID courseId) {
        // Kiểm tra người dùng hiện tại có quyền xem tiến độ học tập không
        User currentUser = securityHelper.getCurrentUser();
        Course course = findCourseById(courseId);

        if (!userId.equals(currentUser.getId())) {
            // Nếu không phải tiến độ của chính mình, kiểm tra xem có phải instructor của khóa học không
            boolean isInstructor = courseInstructorRepository.existsByInstructorIdAndCourseId(
                    currentUser.getId(), courseId);

            if (!isInstructor) {
                throw BadRequestException.message("Bạn không có quyền xem tiến độ học tập của người khác");
            }
        }

        // Lấy danh sách tất cả các bài học của khóa học
        List<Lesson> lessons = course.getSections().stream()
                .flatMap(section -> section.getLessons().stream())
                .toList();

        // Lấy tiến độ học tập
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BadRequestException.message("Không tìm thấy người dùng"));

        List<UserProgress> progressList = userProgressRepository.findByUserAndLessonIn(user, lessons);

        return userProgressMapper.toDtoList(progressList);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProgressDto> getProgressByCourse(UUID courseId, int page, int size) {
        // Kiểm tra người dùng hiện tại có quyền xem tiến độ học tập không
        User currentUser = securityHelper.getCurrentUser();

        // Kiểm tra xem có phải instructor của khóa học không
        boolean isInstructor = courseInstructorRepository.existsByInstructorIdAndCourseId(
                currentUser.getId(), courseId);

        if (!isInstructor) {
            throw BadRequestException.message("Bạn không có quyền xem tiến độ học tập của khóa học này");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserProgress> progressPage = userProgressRepository.findByCourseId(courseId, pageable);

        return progressPage.map(userProgressMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProgressDto> getProgressByLesson(UUID lessonId, int page, int size) {
        // Kiểm tra người dùng hiện tại có quyền xem tiến độ học tập không
        User currentUser = securityHelper.getCurrentUser();
        Lesson lesson = findLessonById(lessonId);

        // Kiểm tra xem có phải instructor của khóa học không
        UUID courseId = lesson.getSection().getCourse().getId();
        boolean isInstructor = courseInstructorRepository.existsByInstructorIdAndCourseId(
                currentUser.getId(), courseId);

        if (!isInstructor) {
            throw BadRequestException.message("Bạn không có quyền xem tiến độ học tập của bài học này");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<UserProgress> progressPage = userProgressRepository.findByLesson(lesson, pageable);

        return progressPage.map(userProgressMapper::toDto);
    }

    @Override
    @Transactional
    public UserProgressDto updateProgress(UUID userId, UUID lessonId, boolean completed, Long lastWatchedTime) {
        // Chỉ cho phép cập nhật tiến độ của chính mình
        User currentUser = securityHelper.getCurrentUser();

        if (!userId.equals(currentUser.getId())) {
            throw BadRequestException.message("Bạn không có quyền cập nhật tiến độ học tập của người khác");
        }

        Lesson lesson = findLessonById(lessonId);
        User user = findUserById(userId);

        // Tìm tiến độ học tập hiện tại hoặc tạo mới
        UserProgress userProgress = userProgressRepository.findByUserAndLesson(user, lesson)
                .orElse(UserProgress.builder()
                        .user(user)
                        .lesson(lesson)
                        .build());

        userProgress.setCompleted(completed);

        if (lastWatchedTime != null) {
            userProgress.setLastWatchedTime(lastWatchedTime);
        }

        userProgress = userProgressRepository.save(userProgress);

        return userProgressMapper.toDto(userProgress);
    }

    private Course findCourseById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy khóa học với id: " + id));
    }

    private Lesson findLessonById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy bài học với id: " + id));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.message("Không tìm thấy người dùng với id: " + id));
    }
}