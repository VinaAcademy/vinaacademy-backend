package com.vinaacademy.platform.feature.review.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity for caching aggregated sentiment statistics for courses
 * Used for dashboard and analytics to avoid real-time calculation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "course_sentiment_statistics", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_course_period", 
            columnNames = {"course_id", "period_type", "period_start"})
    },
    indexes = {
        @Index(name = "idx_period_lookup", 
            columnList = "course_id, period_type, period_start, period_end")
    }
)
public class CourseSentimentStatistics extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    // Time period
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", length = 20, nullable = false)
    private PeriodType periodType;
    
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    // Sentiment counts
    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private Integer totalReviews = 0;
    
    @Column(name = "positive_count", nullable = false)
    @Builder.Default
    private Integer positiveCount = 0;
    
    @Column(name = "neutral_count", nullable = false)
    @Builder.Default
    private Integer neutralCount = 0;
    
    @Column(name = "negative_count", nullable = false)
    @Builder.Default
    private Integer negativeCount = 0;
    
    @Column(name = "mixed_count", nullable = false)
    @Builder.Default
    private Integer mixedCount = 0;
    
    // Average scores
    @Column(name = "avg_positive_score", precision = 5, scale = 4)
    private BigDecimal avgPositiveScore;
    
    @Column(name = "avg_negative_score", precision = 5, scale = 4)
    private BigDecimal avgNegativeScore;
    
    @Column(name = "avg_neutral_score", precision = 5, scale = 4)
    private BigDecimal avgNeutralScore;
    
    // Top aspects stored as JSON
    @Type(JsonType.class)
    @Column(name = "top_pros", columnDefinition = "json")
    private List<String> topPros;
    
    @Type(JsonType.class)
    @Column(name = "top_cons", columnDefinition = "json")
    private List<String> topCons;
    
    @Type(JsonType.class)
    @Column(name = "aspect_breakdown", columnDefinition = "json")
    private Map<String, AspectStats> aspectBreakdown;
    
    /**
     * Nested class for aspect statistics
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AspectStats {
        private Integer mentionCount;
        private Integer positiveCount;
        private Integer negativeCount;
        private Double avgSentimentScore;
    }
    
    /**
     * Enum for period types
     */
    public enum PeriodType {
        DAILY("Hàng ngày"),
        WEEKLY("Hàng tuần"),
        MONTHLY("Hàng tháng"),
        QUARTERLY("Hàng quý"),
        YEARLY("Hàng năm");
        
        private final String displayName;
        
        PeriodType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * Helper method to get sentiment distribution as percentages
     */
    public Map<SentimentType, Double> getSentimentPercentages() {
        Map<SentimentType, Double> percentages = new HashMap<>();
        if (totalReviews == 0) {
            return percentages;
        }
        
        percentages.put(SentimentType.POSITIVE, (positiveCount * 100.0) / totalReviews);
        percentages.put(SentimentType.NEGATIVE, (negativeCount * 100.0) / totalReviews);
        percentages.put(SentimentType.NEUTRAL, (neutralCount * 100.0) / totalReviews);
        percentages.put(SentimentType.MIXED, (mixedCount * 100.0) / totalReviews);
        
        return percentages;
    }
    
    /**
     * Helper method to calculate overall sentiment score (-1 to 1)
     */
    public Double getOverallSentimentScore() {
        if (totalReviews == 0) {
            return 0.0;
        }
        
        double positiveWeight = (positiveCount * 1.0);
        double neutralWeight = (neutralCount * 0.0);
        double negativeWeight = (negativeCount * -1.0);
        double mixedWeight = (mixedCount * 0.0);
        
        return (positiveWeight + neutralWeight + negativeWeight + mixedWeight) / totalReviews;
    }
}
