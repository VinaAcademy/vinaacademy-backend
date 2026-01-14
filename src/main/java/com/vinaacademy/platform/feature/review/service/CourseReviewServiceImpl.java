package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.exception.UnauthorizedException;
import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.dto.CourseReviewRequestDto;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.review.entity.ReviewSentimentAnalysis;
import com.vinaacademy.platform.feature.review.mapper.CourseReviewMapper;
import com.vinaacademy.platform.feature.review.repository.CourseReviewRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import com.vinaacademy.platform.kafka.NotificationProducer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.vinaacademy.kafka.event.NotificationCreateEvent;
import vn.vinaacademy.kafka.event.NotificationCreateEvent.NotificationType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final NotificationProducer notificationProducer;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public CourseReviewDto createOrUpdateReview(UUID userId, CourseReviewRequestDto requestDto) {
        // Validation
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Course course = courseRepository.findById(requestDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + requestDto.getCourseId()));

        if (!isUserEnrolledInCourse(userId, course.getId())) {
            throw new UnauthorizedException();
        }

        CourseReview courseReview;
        Optional<CourseReview> existingReview = courseReviewRepository.findByCourseIdAndUserId(course.getId(), userId);

        if (existingReview.isEmpty()) {
            // Tạo mới
            courseReview = CourseReviewMapper.INSTANCE.toEntity(requestDto, user, course);
            courseReview = courseReviewRepository.save(courseReview);
        } else {
            // Update
            courseReview = existingReview.get();
            int updatedRows = courseReviewRepository.updateReview(
                courseReview.getId(),
                requestDto.getRating(),
                requestDto.getReview(),
                LocalDateTime.now()
            );
            
            if (updatedRows == 0) {
                throw new RuntimeException("Không thể cập nhật đánh giá");
            }
            
            entityManager.flush();
            entityManager.refresh(courseReview);
        }

        // Analyze sentiment và check flag đồng bộ - trả về flag info ngay
        SentimentAnalysisService.ModerationResult moderationResult = 
            sentimentAnalysisService.analyzeAndCheckFlag(courseReview);
        
        // Only update rating and send notifications if review is NOT flagged with negative content
        if (!moderationResult.isHasNegativeFlag()) {
            // Cập nhật rating trung bình
            updateCourseAverageRating(course.getId());
            
            // Send notification to instructor
            UUID instructorId = course.getInstructors().get(0).getInstructor().getId();
            NotificationCreateEvent notification = NotificationCreateEvent.builder()
                .title(user.getFullName() + " đã đánh giá khóa học của bạn")
                .content("Khóa học: " + course.getName())
                .targetUrl("/courses/" + course.getSlug())
                .userId(instructorId)
                .type(NotificationType.SYSTEM)
                .build();
            log.info("send noti review to instructor: {}, slug: {}", instructorId, course.getSlug());
            notificationProducer.sendNotification(notification);
        } else {
            log.warn("Review {} has negative flag, rating update and notifications suppressed", courseReview.getId());
            
            // Automatically hide the review
            int updated = courseReviewRepository.updateHiddenStatus(
                courseReview.getId(),
                true,
                LocalDateTime.now(),
                moderationResult.getFlag().getReason(),
                null
            );
            
            if (updated > 0) {
                entityManager.refresh(courseReview);
                log.info("Automatically hidden review {} due to moderation flag", courseReview.getId());
            }
        }
        
        // Map to DTO and include flag info
        CourseReviewDto dto = CourseReviewMapper.INSTANCE.toDto(courseReview);
        
        // Set flag info from moderation result
        if (moderationResult.getFlag() != null) {
            dto.setFlagType(moderationResult.getFlag().getFlagType());
            dto.setModerationStatus(moderationResult.getFlag().getStatus());
            dto.setFlagSeverity(moderationResult.getFlag().getSeverity());
        }
        
        // Set hidden status
        dto.setIsHidden(courseReview.getIsHidden());
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseReviewDto> getCourseReviews(UUID courseId, Pageable pageable) {
        // Kiểm tra khóa học tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }

        Page<CourseReview> reviewPage = courseReviewRepository.findByCourseId(courseId, pageable);
        return reviewPage.map(CourseReviewMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseReviewDto> getCourseReviewsWithUserPriority(UUID courseId, UUID currentUserId, Pageable pageable) {
        // Kiểm tra khóa học tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }

        if (currentUserId != null) {
            Page<CourseReview> reviewPage = courseReviewRepository.findByCourseIdWithUserPriority(courseId, currentUserId, pageable);
            return reviewPage.map(CourseReviewMapper.INSTANCE::toDto);
        }
        
        // Nếu không có currentUserId, lấy tất cả reviews bình thường
        Page<CourseReview> reviewPage = courseReviewRepository.findByCourseId(courseId, pageable);
        return reviewPage.map(CourseReviewMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseReviewDto> getUserReviews(UUID userId) {
        // Kiểm tra người dùng tồn tại
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId);
        }

        List<CourseReview> reviews = courseReviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(CourseReviewMapper.INSTANCE::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseReviewDto getReviewById(Long reviewId) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        return CourseReviewMapper.INSTANCE.toDto(review);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseReviewDto getUserReviewForCourse(UUID userId, UUID courseId) {
        return courseReviewRepository.findByCourseIdAndUserId(courseId, userId)
                .map(CourseReviewMapper.INSTANCE::toDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteReview(UUID userId, Long reviewId) {
        CourseReview review = courseReviewRepository.findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        UUID courseId = review.getCourse().getId();
        
        // Soft delete: mark as deleted instead of hard delete
        int updated = courseReviewRepository.markAsDeleted(
            reviewId,
            LocalDateTime.now(),
            userId
        );
        
        if (updated == 0) {
            throw new RuntimeException("Không thể xóa đánh giá");
        }

        // Cập nhật đánh giá trung bình của khóa học
        updateCourseAverageRating(courseId);
    }

    @Override
    @Transactional
    public void hideReview(Long reviewId, String reason, UUID moderatorId) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        if (Boolean.TRUE.equals(review.getIsHidden())) {
            log.warn("Đánh giá {} đã bị ẩn từ trước", reviewId);
            return;
        }

        // Cập nhật trạng thái ẩn
        int updated = courseReviewRepository.updateHiddenStatus(
            reviewId,
            true,
            LocalDateTime.now(),
            reason,
            moderatorId
        );

        if (updated == 0) {
            throw new RuntimeException("Không thể ẩn đánh giá");
        }

        // Cập nhật rating trung bình của khóa học (loại trừ review bị ẩn)
        updateCourseAverageRating(review.getCourse().getId());
        
        // Send notification to review owner
        UUID reviewOwnerId = review.getUser().getId();
        NotificationCreateEvent notification = NotificationCreateEvent.builder()
            .title("Đánh giá của bạn đã bị ẩn do vi phạm chuẩn mực")
            .content("Lý do: " + (reason != null ? reason : "Vi phạm tiêu chuẩn cộng đồng"))
            .targetUrl(null)
            .userId(reviewOwnerId)
            .type(NotificationType.SYSTEM)
            .build();
        
        notificationProducer.sendNotification(notification);

        log.info("Đã ẩn đánh giá {} bởi moderator {}. Lý do: {}. Gửi notification cho user {}", 
            reviewId, moderatorId, reason, reviewOwnerId);
    }

    @Override
    @Transactional
    public void markReviewAsDeleted(Long reviewId, UUID moderatorId) {
        CourseReview review = courseReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Đánh dấu là đã xóa (isHidden giữ nguyên - đã là true từ auto-flag)
        int updated = courseReviewRepository.markAsDeleted(
            reviewId,
            LocalDateTime.now(),
            moderatorId
        );

        if (updated == 0) {
            throw new RuntimeException("Không thể đánh dấu xóa đánh giá");
        }

        // Cập nhật rating trung bình của khóa học (loại trừ review bị xóa)
        updateCourseAverageRating(review.getCourse().getId());

        log.info("Đã đánh dấu xóa đánh giá {} bởi moderator {}", reviewId, moderatorId);
    }

    @Override
    @Transactional
    public void unhideReview(Long reviewId, UUID moderatorId) {
        CourseReview review = courseReviewRepository.findHiddenReviewById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Không tìm thấy đánh giá bị ẩn với ID: " + reviewId));

        // Khôi phục review
        int updated = courseReviewRepository.updateHiddenStatus(
            reviewId,
            false,
            null,
            null,
            null
        );

        if (updated == 0) {
            throw new RuntimeException("Không thể khôi phục đánh giá");
        }

        // Cập nhật lại rating trung bình của khóa học
        updateCourseAverageRating(review.getCourse().getId());

        log.info("Đã khôi phục đánh giá {} bởi moderator {}", reviewId, moderatorId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseReviewDto> getHiddenReviews(Pageable pageable) {
        Page<CourseReview> hiddenReviews = courseReviewRepository.findHiddenReviews(pageable);
        return hiddenReviews.map(CourseReviewMapper.INSTANCE::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseReviewStatistics(UUID courseId) {
        // Kiểm tra khóa học tồn tại
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId);
        }

        Map<String, Object> statistics = new HashMap<>();

        // Tính điểm đánh giá trung bình
        Double averageRating = courseReviewRepository.calculateAverageRatingByCourseId(courseId);
        statistics.put("averageRating", averageRating != null ? averageRating : 0.0);

        // Đếm số lượng đánh giá theo điểm (1-5)
        List<Object[]> ratingCounts = courseReviewRepository.countRatingsByCourseId(courseId);
        Map<Integer, Long> ratingDistribution = new HashMap<>();

        // Khởi tạo mặc định là 0 cho tất cả các điểm từ 1-5
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }

        // Cập nhật số lượng thực tế
        for (Object[] result : ratingCounts) {
            Integer rating = ((Number) result[0]).intValue();
            Long count = ((Number) result[1]).longValue();
            ratingDistribution.put(rating, count);
        }

        statistics.put("ratingDistribution", ratingDistribution);

        // Tổng số đánh giá
        long totalReviews = ratingDistribution.values().stream().mapToLong(Long::longValue).sum();
        statistics.put("totalReviews", totalReviews);

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewedCourse(UUID userId, UUID courseId) {
        return courseReviewRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserEnrolledInCourse(UUID userId, UUID courseId) {
        return enrollmentRepository.existsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public boolean isReviewOwnedByUser(Long reviewId, UUID userId) {
        return courseReviewRepository.existsByIdAndUserId(reviewId, userId);
    }

    private void updateCourseAverageRating(UUID courseId) {
        Double averageRating = courseReviewRepository.calculateAverageRatingByCourseId(courseId);
        Long totalReviews = courseReviewRepository.countByCourseId(courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

        course.setRating(averageRating != null ? averageRating : 0.0);
        course.setTotalRating(totalReviews);
        courseRepository.save(course);
    }
}
