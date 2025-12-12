package com.vinaacademy.platform.feature.review.dto.sentiment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for moderating a flagged review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationActionRequest {
    
    @NotBlank(message = "Action is required")
    private String action; // "approve", "reject"
    
    private String notes;
    private Boolean deleteReview; // If approve, should we also delete the review?
}
