package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.exception.UnauthorizedException;
import com.vinaacademy.platform.feature.common.exception.ResourceNotFoundException;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.repository.CourseRepository;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.dto.CourseReviewRequestDto;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.review.mapper.CourseReviewMapper;
import com.vinaacademy.platform.feature.review.repository.CourseReviewRepository;
import com.vinaacademy.platform.feature.user.UserRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CourseReviewDto createOrUpdateReview(UUID userId, CourseReviewRequestDto requestDto) {
        // Validation (giữ nguyên)
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
            // Tạo mới - dùng save()
            courseReview = CourseReviewMapper.INSTANCE.toEntity(requestDto, user, course);
            courseReview = courseReviewRepository.save(courseReview);
        } else {
            // Update - dùng update query riêng
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
            
            // Refresh entity để có data mới nhất
            courseReview = courseReviewRepository.findById(courseReview.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá sau khi cập nhật"));
        }

        // Cập nhật rating trung bình
        updateCourseAverageRating(course.getId());

        return CourseReviewMapper.INSTANCE.toDto(courseReview);
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
        courseReviewRepository.delete(review);

        // Cập nhật đánh giá trung bình của khóa học
        updateCourseAverageRating(courseId);
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

        if (averageRating != null) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khóa học với ID: " + courseId));

            course.setRating(averageRating);
            courseRepository.save(course);
        }
    }
}
