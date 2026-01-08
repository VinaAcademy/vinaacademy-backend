package com.vinaacademy.platform.feature.discussion.dto.moderation;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for moderating a flagged discussion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionModerationActionRequest {
    
    @NotBlank(message = "Action is required")
    private String action; // "approve", "reject"
    
    private String notes;
    private Boolean hideDiscussion; // If approve, should we also hide the discussion?
}
