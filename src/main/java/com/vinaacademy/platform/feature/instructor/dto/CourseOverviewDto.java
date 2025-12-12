package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho tổng quan khóa học của giảng viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOverviewDto {

    private Summary summary;
    private List<CourseDetail> courses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalCourses;
        private Double averageCompletionRate;
        private Long totalStudents;
        private BigDecimal totalRevenue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseDetail {
        private UUID id;
        private String name;
        private Long students;
        private Double rating;
        private Long totalReviews;
        private BigDecimal revenue;
        private BigDecimal price;
        private Double completionRate;
        private LocalDateTime lastUpdated;
        private String status;
    }
}
