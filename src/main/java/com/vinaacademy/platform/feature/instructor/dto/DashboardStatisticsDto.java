package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê dashboard giảng viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDto {

    private RevenueStats revenue;
    private StudentStats newStudents;
    private RatingStats averageRating;
    private Integer totalCourses;
    private Double completionRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStats {
        private BigDecimal current;      // Doanh thu kỳ hiện tại
        private Double change;           // % thay đổi so với kỳ trước
        private Boolean isIncrease;      // True nếu tăng, false nếu giảm
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStats {
        private Long current;            // Số học viên mới kỳ hiện tại
        private Double change;           // % thay đổi so với kỳ trước
        private Boolean isIncrease;      // True nếu tăng, false nếu giảm
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingStats {
        private Double current;          // Đánh giá trung bình hiện tại
        private Long totalReviews;       // Tổng số đánh giá
    }
}
