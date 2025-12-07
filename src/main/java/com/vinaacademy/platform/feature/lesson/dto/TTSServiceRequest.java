package com.vinaacademy.platform.feature.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for calling Text-to-Speech Service
 * Maps to TTSRequest in text-to-speech-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TTSServiceRequest {
    
    /**
     * Text content to synthesize
     */
    private String text;
    
    /**
     * Voice name (e.g., "vi-VN-HoaiMyNeural")
     */
    private String voice;
    
    /**
     * Language code (e.g., "vi-VN")
     */
    private String language;
    
    /**
     * Speech speed (e.g., "1.0")
     */
    private String speed;
    
    /**
     * Pitch adjustment (e.g., "0Hz")
     */
    private String pitch;
    
    /**
     * Course ID for tracking
     */
    private String courseId;
    
    /**
     * Section ID for tracking
     */
    private String sectionId;
    
    /**
     * Lesson ID for tracking
     */
    private String lessonId;
}
