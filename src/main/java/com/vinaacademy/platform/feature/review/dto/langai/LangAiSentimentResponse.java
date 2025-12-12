package com.vinaacademy.platform.feature.review.dto.langai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO from Lang-AI Service sentiment analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangAiSentimentResponse {
    
    private String text;
    private String sentiment; // "positive", "negative", "neutral", "mixed"
    private ScoresDto scores;
    private List<SentenceSentiment> sentences;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoresDto {
        private BigDecimal positive;
        private BigDecimal neutral;
        private BigDecimal negative;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SentenceSentiment {
        private String text;
        private String sentiment;
        private ScoresDto scores;
    }
}
