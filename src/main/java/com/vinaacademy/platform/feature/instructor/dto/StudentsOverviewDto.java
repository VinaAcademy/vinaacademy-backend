package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho tổng quan học viên của instructor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentsOverviewDto {

    private StudentStats totalStudents;
    private StudentStats newStudents;
    private CompletionStats completionStats;
    private List<CourseStudentCount> topCoursesByStudents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentStats {
        private Long count;
        private BigDecimal growthRate; // Tỷ lệ tăng trưởng so với kỳ trước (%)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletionStats {
        private Long inProgress;
        private Long completed;
        private Long notStarted;
        private Double averageCompletionRate; // Tỷ lệ hoàn thành trung bình (%)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStudentCount {
        private String courseId;
        private String courseTitle;
        private String courseThumbnail;
        private Long studentCount;
    }
}
