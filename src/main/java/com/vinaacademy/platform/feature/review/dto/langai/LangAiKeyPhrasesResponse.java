package com.vinaacademy.platform.feature.review.dto.langai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO from Lang-AI Service key phrases extraction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LangAiKeyPhrasesResponse {
    
    private String text;
    private List<String> keyPhrases;
    private String language;
}
