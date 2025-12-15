package com.vinaacademy.platform.feature.review.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.review.enums.AspectCategory;
import com.vinaacademy.platform.feature.review.enums.PhraseType;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

/**
 * Entity representing key phrases extracted from a review
 * Used to identify pros, cons, and specific aspects mentioned
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_key_phrases", indexes = {
    @Index(name = "idx_review_phrases", columnList = "review_id, phrase_type"),
    @Index(name = "idx_category_sentiment", columnList = "category, sentiment"),
    @Index(name = "idx_phrase_type", columnList = "phrase_type")
})
public class ReviewKeyPhrase extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CourseReview review;
    
    @Column(name = "phrase", columnDefinition = "TEXT", nullable = false)
    private String phrase;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "phrase_type", length = 20, nullable = false)
    private PhraseType phraseType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", length = 20)
    private SentimentType sentiment;
    
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;
    
    // Categorization of the aspect being discussed
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private AspectCategory category;
    
    /**
     * Helper method to check if this is a pro (positive aspect)
     */
    public boolean isPro() {
        return phraseType == PhraseType.PRO;
    }
    
    /**
     * Helper method to check if this is a con (negative aspect)
     */
    public boolean isCon() {
        return phraseType == PhraseType.CON;
    }
    
    /**
     * Helper method to check if this phrase has high confidence
     */
    public boolean isHighConfidence() {
        return confidence != null && confidence.compareTo(new BigDecimal("0.7")) >= 0;
    }
}
