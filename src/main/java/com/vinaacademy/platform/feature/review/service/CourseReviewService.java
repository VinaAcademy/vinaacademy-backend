package com.vinaacademy.platform.feature.review.service;

import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.dto.CourseReviewRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CourseReviewService {
    //Tạo đánh giá mới hoặc cập nhật đánh giá hiện có cho khóa học
    CourseReviewDto createOrUpdateReview(UUID userId, CourseReviewRequestDto requestDto);

    //Lấy tất cả đánh giá của một khóa học với phân trang
    Page<CourseReviewDto> getCourseReviews(UUID courseId, Pageable pageable);

    //Lấy tất cả đánh giá của một người dùng
    List<CourseReviewDto> getUserReviews(UUID userId);

    //Lấy thông tin của một đánh giá cụ thể
    CourseReviewDto getReviewById(Long reviewId);

    //Lấy đánh giá của một người dùng cho một khóa học cụ thể
    CourseReviewDto getUserReviewForCourse(UUID userId, UUID courseId);

    //Xóa một đánh giá
    void deleteReview(UUID userId, Long reviewId);

    //Lấy thống kê đánh giá của một khóa học
    Map<String, Object> getCourseReviewStatistics(UUID courseId);

    //Kiểm tra người dùng đã đánh giá khóa học chưa
    boolean hasUserReviewedCourse(UUID userId, UUID courseId);

    //Kiểm tra người dùng đã đăng ký khóa học chưa
    boolean isUserEnrolledInCourse(UUID userId, UUID courseId);

    //Kiểm tra đánh giá có thuộc về người dùng hay không
    boolean isReviewOwnedByUser(Long reviewId, UUID userId);
}
