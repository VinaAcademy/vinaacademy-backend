package com.vinaacademy.platform.feature.enrollment.dto;

import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.enrollment.enums.ProgressStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {
    private Long id;
    private UUID userId;
    private UUID courseId;
    private String courseName;
    private String courseImage;
    private CourseStatus courseStatus;
    private String courseSlug;
    private Double progressPercentage;
    private ProgressStatus status;
    private LocalDateTime startAt;
    private LocalDateTime completeAt;
    private long completedLessons;
    private long totalLessons;
    private String category;
}