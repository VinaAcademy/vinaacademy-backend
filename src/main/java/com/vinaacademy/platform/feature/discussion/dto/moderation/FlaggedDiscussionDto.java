package com.vinaacademy.platform.feature.discussion.dto.moderation;

import com.vinaacademy.platform.feature.discussion.dto.DiscussionDto;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for flagged discussion information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedDiscussionDto {
    
    private Long flagId;
    private DiscussionDto discussion;
    private FlagType flagType;
    private Integer severity;
    private BigDecimal confidence;
    private String reason;
    private ModerationStatus status;
    private LocalDateTime flaggedAt;
    private LocalDateTime reviewedAt;
    private String moderatorNotes;
    
    // Additional info
    private String courseName;
    private String lessonTitle;
}
