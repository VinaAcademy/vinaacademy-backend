package com.vinaacademy.platform.feature.review.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing sentiment analysis results for a course review
 * Stores detailed sentiment scores and toxicity detection from Azure AI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_sentiment_analysis", indexes = {
    @Index(name = "idx_review_sentiment", columnList = "review_id, sentiment"),
    @Index(name = "idx_toxic_reviews", columnList = "is_toxic, sentiment"),
    @Index(name = "idx_analyzed_at", columnList = "analyzed_at")
})
public class ReviewSentimentAnalysis extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private CourseReview review;
    
    // Overall Sentiment
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", length = 20, nullable = false)
    private SentimentType sentiment;
    
    @Column(name = "confidence_positive", precision = 5, scale = 4, nullable = false)
    private BigDecimal confidencePositive;
    
    @Column(name = "confidence_neutral", precision = 5, scale = 4, nullable = false)
    private BigDecimal confidenceNeutral;
    
    @Column(name = "confidence_negative", precision = 5, scale = 4, nullable = false)
    private BigDecimal confidenceNegative;
    
    // Toxicity Detection (for moderation)
    @Column(name = "is_toxic")
    @Builder.Default
    private Boolean isToxic = false;
    
    @Column(name = "toxicity_score", precision = 5, scale = 4)
    private BigDecimal toxicityScore;
    
    // Analysis Metadata
    @Column(name = "language_detected", length = 10)
    private String languageDetected;
    
    @Column(name = "analysis_version", length = 20)
    private String analysisVersion;
    
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    // Soft delete support
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * Helper method to check if this is a high-confidence negative review
     */
    public boolean isHighConfidenceNegative() {
        return sentiment == SentimentType.NEGATIVE 
            && confidenceNegative.compareTo(new BigDecimal("0.8")) >= 0;
    }
    
    /**
     * Helper method to get the dominant sentiment score
     */
    public BigDecimal getDominantScore() {
        return switch (sentiment) {
            case POSITIVE -> confidencePositive;
            case NEGATIVE -> confidenceNegative;
            case NEUTRAL -> confidenceNeutral;
            case MIXED -> confidencePositive.max(confidenceNegative).max(confidenceNeutral);
        };
    }
}
