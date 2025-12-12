package com.vinaacademy.platform.feature.review.dto.sentiment;

import com.vinaacademy.platform.feature.review.dto.CourseReviewDto;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for review with sentiment analysis data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewWithSentimentDto {
    
    private CourseReviewDto review;
    private ReviewSentimentDto sentiment;
    private List<KeyPhraseDto> keyPhrases;
    private Boolean isFlagged;
    
    /**
     * Helper method to get sentiment type directly
     */
    public SentimentType getSentimentType() {
        return sentiment != null ? sentiment.getSentiment() : null;
    }
}
