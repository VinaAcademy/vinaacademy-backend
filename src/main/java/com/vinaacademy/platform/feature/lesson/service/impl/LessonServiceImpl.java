package com.vinaacademy.platform.feature.lesson.service.impl;

import com.vinaacademy.platform.exception.BadRequestException;
import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.course.enums.LessonType;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.course.repository.UserProgressRepository;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.enrollment.service.EnrollmentService;
import com.vinaacademy.platform.feature.lesson.dto.LessonDto;
import com.vinaacademy.platform.feature.lesson.dto.LessonRequest;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.entity.UserProgress;
import com.vinaacademy.platform.feature.lesson.factory.LessonCreator;
import com.vinaacademy.platform.feature.lesson.factory.LessonCreatorFactory;
import com.vinaacademy.platform.feature.lesson.mapper.LessonMapper;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.service.LessonService;
import com.vinaacademy.platform.feature.log.service.LogService;
import com.vinaacademy.platform.feature.section.entity.Section;
import com.vinaacademy.platform.feature.section.repository.SectionRepository;
import com.vinaacademy.platform.feature.user.auth.annotation.RequiresResourcePermission;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.auth.service.AuthorizationService;
import com.vinaacademy.platform.feature.user.constant.ResourceConstants;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final AuthorizationService authorizationService;
    private final LogService logService;
    private final UserProgressRepository userProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    @Autowired
    private LessonMapper lessonMapper;
    @Autowired
    private LessonCreatorFactory lessonCreatorFactory;
    @Autowired
    private SecurityHelper securityHelper;

    @Override
    @Transactional(readOnly = true)
    public LessonDto getLessonById(UUID id) {
        log.debug("Getting lesson by id: {}", id);
        Lesson lesson = findLessonById(id);
        return lessonMapper.lessonToLessonDto(lesson);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsBySectionId(UUID sectionId) {
        log.debug("Getting lessons by section id: {}", sectionId);
        Section section = findSectionById(sectionId);
        return lessonRepository.findBySectionOrderByOrderIndex(section).stream()
                .map(lessonMapper::lessonToLessonDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @RequiresResourcePermission(
            resourceType = ResourceConstants.SECTION,
            permission = ResourceConstants.VIEW_OWN,
            idParam = "request.sectionId"
    )
    public LessonDto createLesson(LessonRequest request) {
        log.info("Creating new lesson with title: {}, type: {}", request.getTitle(), request.getType());
        User currentUser = securityHelper.getCurrentUser();
        Section section = findSectionById(request.getSectionId());

        // Luôn tạo lesson mới ở cuối danh sách
        List<Lesson> existingLessons = lessonRepository.findBySectionOrderByOrderIndex(section);
        request.setOrderIndex(existingLessons.size()); // Đặt ở vị trí cuối cùng

        // Tạo lesson mới
        LessonDto newLesson = createLesson(request, currentUser);

        // Cập nhật trạng thái khóa học nếu cần
        updateCourseStatusAfterAddingLesson(section.getCourse());

        return newLesson;
    }

    /**
     * Cập nhật trạng thái khóa học sau khi thêm bài học mới
     * - Nếu trạng thái hiện tại là DRAFT hoặc PENDING, giữ nguyên
     * - Nếu trạng thái hiện tại là REJECTED hoặc PUBLISHED, chuyển thành PENDING
     *
     * @param course Khóa học cần cập nhật trạng thái
     */
    private void updateCourseStatusAfterAddingLesson(Course course) {
        CourseStatus currentStatus = course.getStatus();

        // Chỉ thay đổi trạng thái nếu là REJECTED hoặc PUBLISHED
        if (currentStatus == CourseStatus.REJECTED || currentStatus == CourseStatus.PUBLISHED) {
            course.setStatus(CourseStatus.PENDING);

            // Ghi log việc thay đổi trạng thái
            log.info("Course status changed from {} to PENDING due to new lesson addition. Course ID: {}",
                    currentStatus, course.getId());
        }
        course.setTotalLesson(course.getTotalLesson() + 1);
        courseRepository.save(course);
    }

    @Override
    @Transactional
    @RequiresResourcePermission(
            resourceType = ResourceConstants.SECTION,
            permission = ResourceConstants.VIEW_OWN,
            idParam = "request.sectionId"
    )
    public LessonDto createLesson(LessonRequest request, User author) {
        log.info("Creating new lesson with title: {}, type: {} by explicit author: {}",
                request.getTitle(), request.getType(), author.getUsername());
        Section section = findSectionById(request.getSectionId());

        validateOrderIndex(request.getOrderIndex(), section, null);

        // Get the appropriate creator for this lesson type
        LessonCreator creator = lessonCreatorFactory.getCreator(request.getType());

        // Use the factory method to create the lesson
        Lesson lesson = creator.createLesson(request, section, author);

        // Log the creation
        logService.log("Lesson", "CREATE",
                String.format("Created new %s lesson in section: %s",
                        request.getType(), section.getTitle()),
                null, lessonMapper.lessonToLessonDto(lesson));

        return lessonMapper.lessonToLessonDto(lesson);
    }

    @Override
    @Transactional
    @RequiresResourcePermission(
            resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.EDIT,
            idParam = "id"
    )
    public LessonDto updateLesson(UUID id, LessonRequest request) {
        log.info("Updating lesson with id: {}", id);
        Lesson existingLesson = findLessonById(id);
        Section section = findSectionById(request.getSectionId());
        Course course = section.getCourse();

        LessonDto oldLessonData = lessonMapper.lessonToLessonDto(existingLesson);

        // Check if user has permission using AuthorizationService
        if (!authorizationService.canModifyResource(existingLesson.getAuthor().getId())) {
            throw new ValidationException("You don't have permission to update this lesson");
        }

        validateLessonRequest(request);
        validateOrderIndex(request.getOrderIndex(), section, id);

        // Basic update for common fields
        existingLesson.setTitle(request.getTitle());
        existingLesson.setDescription(request.getDescription());
        existingLesson.setSection(section);
        existingLesson.setFree(request.isFree());
        existingLesson.setOrderIndex(request.getOrderIndex());

        // Specific updates based on lesson type
        if (existingLesson.getType() != request.getType()) {
            throw new ValidationException("Cannot change lesson type. Delete and create a new lesson instead.");
        }

        updateLessonByType(existingLesson, request);

        // Cập nhật trạng thái khóa học sau khi cập nhật bài học
        boolean isQuizWithSettings = LessonType.QUIZ.equals(request.getType())
                && request.getSettings() != null
                && !request.getSettings().isEmpty();
        if (!isQuizWithSettings) {
            updateCourseStatusAfterModifyingLessons(course);
        }


        // Log the update
        logService.log("Lesson", "UPDATE",
                String.format("Updated %s lesson in section: %s",
                        request.getType(), section.getTitle()),
                oldLessonData, lessonMapper.lessonToLessonDto(existingLesson));

        return lessonMapper.lessonToLessonDto(existingLesson);
    }

    @Override
    @Transactional
    @RequiresResourcePermission(
            resourceType = ResourceConstants.LESSON,
            permission = ResourceConstants.DELETE,
            idParam = "id"
    )
    public void deleteLesson(UUID id) {
        log.info("Deleting lesson with id: {}", id);
        Lesson lesson = findLessonById(id);
        Section section = lesson.getSection();
        Course course = section.getCourse();
        int deletedOrderIndex = lesson.getOrderIndex();

        // Check if user has permission using AuthorizationService
        if (!authorizationService.canModifyResource(lesson.getAuthor().getId())) {
            throw new ValidationException("You don't have permission to delete this lesson");
        }

        LessonDto lessonData = lessonMapper.lessonToLessonDto(lesson);

        lessonRepository.delete(lesson);

        // Cập nhật lại orderIndex cho các lesson sau lesson bị xóa
        List<Lesson> lessonsToUpdate = lessonRepository.findBySectionOrderByOrderIndex(section).stream()
                .filter(l -> l.getOrderIndex() > deletedOrderIndex)
                .collect(Collectors.toList());

        for (Lesson lessonToUpdate : lessonsToUpdate) {
            lessonToUpdate.setOrderIndex(lessonToUpdate.getOrderIndex() - 1);
            lessonRepository.save(lessonToUpdate);
        }

        // Cập nhật trạng thái khóa học sau khi xóa bài học
        updateCourseStatusAfterModifyingLessons(course);

        // Log the deletion
        logService.log("Lesson", "DELETE",
                String.format("Deleted %s lesson: %s",
                        lesson.getType(), lesson.getTitle()),
                lessonData, null);
    }

    /**
     * Cập nhật trạng thái khóa học sau khi thay đổi bài học (thêm, xóa, sắp xếp lại)
     * - Nếu trạng thái hiện tại là DRAFT hoặc PENDING, giữ nguyên
     * - Nếu trạng thái hiện tại là REJECTED hoặc PUBLISHED, chuyển thành PENDING
     *
     * @param course Khóa học cần cập nhật trạng thái
     */
    private void updateCourseStatusAfterModifyingLessons(Course course) {
        CourseStatus currentStatus = course.getStatus();

        // Chỉ thay đổi trạng thái nếu là REJECTED hoặc PUBLISHED
        if (currentStatus == CourseStatus.REJECTED || currentStatus == CourseStatus.PUBLISHED) {
            course.setStatus(CourseStatus.PENDING);
            courseRepository.save(course);

            // Ghi log việc thay đổi trạng thái
            log.info("Course status changed from {} to PENDING due to lesson modification. Course ID: {}",
                    currentStatus, course.getId());
        }
    }

    @Transactional
    @Override
    @RequiresResourcePermission(resourceType = ResourceConstants.LESSON, idParam = "lessonId")
    public void completeLesson(UUID lessonId) {
        User currentUser = securityHelper.getCurrentUser();
        Lesson lesson = findLessonById(lessonId);

        if (lesson.getType() == LessonType.QUIZ) {
            throw BadRequestException.message("Bài học này là bài kiểm tra, không thể đánh dấu hoàn thành");
        }

        markLessonCompleted(lesson, currentUser);
    }

    public void markLessonCompleted(Lesson lesson, User currentUser) {
        // 1. Mark lesson completed if not already
        Optional<UserProgress> userProgressOpt = userProgressRepository
                .findByLessonIdAndUserId(lesson.getId(), currentUser.getId())
                .filter(UserProgress::isCompleted);
        if (userProgressOpt.isPresent()) {
            throw BadRequestException.message("Học viên đã hoàn thành bài học này");
        }

        UserProgress userProgress = userProgressOpt.orElseGet(() ->
                userProgressRepository.save(UserProgress.builder()
                        .lesson(lesson)
                        .user(currentUser)
                        .completed(true)
                        .build())
        );
        userProgressRepository.save(userProgress);

        // 2. Update enrollment progress
        UUID courseId = lesson.getSection().getCourse().getId();
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(currentUser.getId(), courseId)
                .orElseThrow(() -> BadRequestException.message("Học viên chưa đăng ký khóa học này"));

        long totalLessons = lessonRepository.countBySectionCourseId(courseId);
        long completedLessons = userProgressRepository.countCompletedByUserIdAndCourseId(currentUser.getId(), courseId);

        if (totalLessons == 0) {
            throw new ValidationException("Không thể tính tiến độ: khóa học không có bài học nào");
        }

        double progressPercentage = (completedLessons * 1.0 / totalLessons) * 100;

        enrollment.setProgressPercentage(progressPercentage);
        enrollment.setStatus(progressPercentage >= 100 ? ProgressStatus.COMPLETED : ProgressStatus.IN_PROGRESS);
        enrollment.setCompleteAt(progressPercentage >= 100 ? LocalDateTime.now() : null);
        enrollment.setCompletedLessons(completedLessons);

        enrollmentRepository.save(enrollment);
    }

    private Lesson findLessonById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học with id: " + id));
    }

    private Section findSectionById(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Section not found with id: " + id));
    }

    private void validateLessonRequest(LessonRequest request) {
        // Common validations
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ValidationException("Lesson title cannot be empty");
        }

        if (request.getTitle().length() > 255) {
            throw new ValidationException("Lesson title cannot exceed 255 characters");
        }

        if (request.getOrderIndex() < 0) {
            throw new ValidationException("Order index cannot be negative");
        }

        // Type-specific validations
        switch (request.getType()) {
            case VIDEO:
                break;

            case READING:
                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                    throw new ValidationException("Content is required for reading lessons");
                }
                break;

            case QUIZ:
//                if (request.getPassPoint() == null || request.getTotalPoint() == null || request.getDuration() == null) {
//                    throw new ValidationException("Pass point, total point, and duration are required for quiz lessons");
//                }
//
//                if (request.getPassPoint() < 0 || request.getTotalPoint() < 0) {
//                    throw new ValidationException("Points cannot be negative");
//                }
//
//                if (request.getPassPoint() > request.getTotalPoint()) {
//                    throw new ValidationException("Pass point cannot be greater than total point");
//                }
//
//                if (request.getDuration() <= 0) {
//                    throw new ValidationException("Duration must be positive");
//                }
                break;

            default:
                throw new ValidationException("Unsupported lesson type: " + request.getType());
        }
    }

    /**
     * Validates that the order index is appropriate for the section
     *
     * @param orderIndex the requested order index
     * @param section    the section where the lesson belongs
     * @param lessonId   the ID of the lesson being updated (null for creation)
     */
    // For LessonServiceImpl.java
    private void validateOrderIndex(int orderIndex, Section section, UUID lessonId) {
        List<Lesson> existingLessons = lessonRepository.findBySectionOrderByOrderIndex(section);

        // For updates, exclude the current lesson from duplicate check
        if (lessonId != null) {
            existingLessons = existingLessons.stream()
                    .filter(lesson -> !lesson.getId().equals(lessonId))
                    .toList();
        }

        // Calculate the expected maximum order index
        int maxAllowedIndex = existingLessons.size();
        if (lessonId != null) {
            // When updating, we can use the same index or one more than the current size
            maxAllowedIndex++;
        }

        // Ensure the order index is within valid range
        if (orderIndex < 0 || orderIndex > maxAllowedIndex) {
            throw new ValidationException(
                    String.format("Order index %d is invalid. Maximum allowed is %d for section '%s'",
                            orderIndex, maxAllowedIndex, section.getTitle()));
        }

        // If the orderIndex is already used, shift the existing ones
        boolean isOrderIndexUsed = existingLessons.stream()
                .anyMatch(lesson -> lesson.getOrderIndex() == orderIndex);

        if (isOrderIndexUsed) {
            // Shift existing lessons from the insertion point onwards
            List<Lesson> lessonsToUpdate = existingLessons.stream()
                    .filter(lesson -> lesson.getOrderIndex() >= orderIndex)
                    .collect(Collectors.toList());

            for (Lesson lesson : lessonsToUpdate) {
                lesson.setOrderIndex(lesson.getOrderIndex() + 1);
                lessonRepository.save(lesson);
            }
        }
    }

    private void updateLessonByType(Lesson lesson, LessonRequest request) {
        // Validate that we're not trying to change the lesson type
        if (lesson.getType() != request.getType()) {
            throw new ValidationException("Cannot change lesson type. Delete and create a new lesson instead.");
        }

        // Get the appropriate creator for this lesson type using our factory
        LessonCreator creator = lessonCreatorFactory.getCreator(lesson.getType());

        // Use the creator to update the lesson
        creator.updateLesson(lesson, request);
    }
}
