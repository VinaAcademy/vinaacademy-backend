package com.vinaacademy.platform.feature.review.dto.sentiment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for sentiment confidence scores
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentScoresDto {
    
    private BigDecimal positive;
    private BigDecimal neutral;
    private BigDecimal negative;
    
    /**
     * Get the dominant (highest) score
     */
    public BigDecimal getDominantScore() {
        return positive.max(neutral).max(negative);
    }
}
