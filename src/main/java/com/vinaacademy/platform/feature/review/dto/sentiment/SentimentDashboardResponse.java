package com.vinaacademy.platform.feature.review.dto.sentiment;

import com.vinaacademy.platform.feature.review.enums.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for instructor sentiment dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentimentDashboardResponse {
    
    private UUID courseId;
    private String courseName;
    private PeriodInfo period;
    private OverviewStats overview;
    private List<TrendDataPoint> sentimentTrend;
    private List<AspectAnalysis> topAspects;
    private List<String> recentImprovements;
    private List<String> recentConcerns;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodInfo {
        private LocalDate startDate;
        private LocalDate endDate;
        private String periodType;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private Integer totalReviews;
        private Map<SentimentType, Integer> sentimentDistribution;
        private Map<SentimentType, Double> sentimentPercentages;
        private Double overallSentimentScore; // -1 to 1
        private Integer toxicReviewsCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        private LocalDate date;
        private Integer positiveCount;
        private Integer negativeCount;
        private Integer neutralCount;
        private Integer mixedCount;
        private Double sentimentScore;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AspectAnalysis {
        private String category;
        private Integer totalMentions;
        private List<String> topPros;
        private List<String> topCons;
        private Double avgSentimentScore;
        private String trend; // "improving", "declining", "stable"
    }
}
