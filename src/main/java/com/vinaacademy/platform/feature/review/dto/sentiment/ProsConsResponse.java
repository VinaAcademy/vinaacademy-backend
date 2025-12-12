package com.vinaacademy.platform.feature.review.dto.sentiment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for pros/cons summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProsConsResponse {
    
    private UUID courseId;
    private String courseName;
    private Integer totalReviews;
    private List<KeyPhraseItem> pros;
    private List<KeyPhraseItem> cons;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyPhraseItem {
        private String phrase;
        private Integer count;
        private String category;
        private Double averageConfidence;
    }
}
