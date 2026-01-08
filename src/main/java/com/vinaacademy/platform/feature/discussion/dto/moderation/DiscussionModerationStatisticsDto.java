package com.vinaacademy.platform.feature.discussion.dto.moderation;

import com.vinaacademy.platform.feature.review.enums.FlagType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for discussion moderation statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionModerationStatisticsDto {
    
    private Long totalPendingFlags;
    private Long totalApprovedFlags;
    private Long totalRejectedFlags;
    private Long criticalPendingFlags;
    
    // Count by flag type for pending flags
    private Map<FlagType, Long> pendingByType;
    
    // Average processing time in hours
    private Double averageProcessingTimeHours;
}
