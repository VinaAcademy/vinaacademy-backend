package com.vinaacademy.platform.feature.review.dto.sentiment;

import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for flagged review information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedReviewDto {
    
    private Long flagId;
    private CourseReviewDto review;
    private ReviewSentimentDto sentiment;
    private FlagType flagType;
    private Integer severity;
    private BigDecimal confidence;
    private String reason;
    private ModerationStatus status;
    private LocalDateTime flaggedAt;
    private UUID reviewedBy;
    private LocalDateTime reviewedAt;
    private String moderatorNotes;
}
