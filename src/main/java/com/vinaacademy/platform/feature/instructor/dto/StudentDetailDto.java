package com.vinaacademy.platform.feature.instructor.dto;

import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO cho thông tin chi tiết học viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailDto {

    private UUID userId;
    private String fullName;
    private String email;
    private String avatar;
    private List<CourseEnrollment> enrollments;
    private LocalDateTime lastActive;
    private Integer totalCoursesEnrolled;
    private Double averageProgress;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseEnrollment {
        private Long enrollmentId;
        private UUID courseId;
        private String courseTitle;
        private String courseThumbnail;
        private ProgressStatus status;
        private Double progressPercentage;
        private LocalDateTime enrolledAt;
        private LocalDateTime lastAccessedAt;
        private LocalDateTime completedAt;
    }
}
