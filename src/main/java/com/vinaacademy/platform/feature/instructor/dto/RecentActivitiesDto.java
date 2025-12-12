package com.vinaacademy.platform.feature.instructor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho hoạt động gần đây của giảng viên
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivitiesDto {

    private List<EnrollmentActivity> recentEnrollments;
    private List<ReviewActivity> recentReviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentActivity {
        private Long id;
        private String studentName;
        private String studentAvatar;
        private String courseName;
        private LocalDateTime enrolledAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewActivity {
        private Long id;
        private String studentName;
        private String studentAvatar;
        private String courseName;
        private Integer rating;
        private String comment;
        private LocalDateTime reviewedAt;
    }
}
