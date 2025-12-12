package com.vinaacademy.platform.feature.review.dto.sentiment;

import com.vinaacademy.platform.feature.review.enums.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for review sentiment analysis data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSentimentDto {
    
    private Long id;
    private Long reviewId;
    private SentimentType sentiment;
    private SentimentScoresDto scores;
    private Boolean isToxic;
    private BigDecimal toxicityScore;
    private String languageDetected;
    private LocalDateTime analyzedAt;
}
