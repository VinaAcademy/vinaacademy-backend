package com.vinaacademy.platform.feature.review.dto.langai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Lang-AI Service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangAiAnalysisRequest {
    
    private String text;
    private String language; // "vi", "en", or null for auto-detect
}
