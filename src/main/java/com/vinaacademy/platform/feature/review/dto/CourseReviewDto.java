package com.vinaacademy.platform.feature.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;

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
    
    // Moderation fields
    private Boolean isHidden;
    private FlagType flagType;
    private ModerationStatus moderationStatus;
    private Integer flagSeverity;
}
