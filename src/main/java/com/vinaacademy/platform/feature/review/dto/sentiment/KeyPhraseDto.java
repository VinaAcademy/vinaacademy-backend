package com.vinaacademy.platform.feature.review.dto.sentiment;

import com.vinaacademy.platform.feature.review.enums.AspectCategory;
import com.vinaacademy.platform.feature.review.enums.PhraseType;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for key phrases extracted from reviews
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyPhraseDto {
    
    private Long id;
    private Long reviewId;
    private String phrase;
    private PhraseType phraseType;
    private SentimentType sentiment;
    private BigDecimal confidence;
    private AspectCategory category;
}
