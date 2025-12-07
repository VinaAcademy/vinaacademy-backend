package com.vinaacademy.platform.feature.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal DTO for Text-to-Speech Service response
 * Maps to TTSResponse from text-to-speech-service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TTSServiceResponse {
    
    /**
     * Audio data as base64 string
     */
    private String audioBase64;
    
    /**
     * Audio URL (if applicable)
     */
    private String audioUrl;
    
    /**
     * Audio format (e.g., "audio/mpeg")
     */
    private String format;
    
    /**
     * Duration in milliseconds
     */
    private Long duration;
    
    /**
     * Status: "success" or "error"
     */
    private String status;
    
    /**
     * Error message if status is error
     */
    private String message;
}
