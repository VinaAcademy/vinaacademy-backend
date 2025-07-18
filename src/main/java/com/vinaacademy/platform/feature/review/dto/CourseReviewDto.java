package com.vinaacademy.platform.feature.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewDto {
    private Long id;
    private UUID courseId;
    private String courseName;
    private int rating;
    private String review;
    private UUID userId;
    private String userFullName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
