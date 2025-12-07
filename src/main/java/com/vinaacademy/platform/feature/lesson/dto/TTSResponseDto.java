package com.vinaacademy.platform.feature.lesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for Text-to-Speech response to client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TTSResponseDto {
    
    /**
     * Audio data encoded as base64 string
     * Only populated when format is "base64"
     */
    private String audioBase64;
    
    /**
     * URL to audio file (if saved to storage)
     * Alternative to audioBase64 for large files
     */
    private String audioUrl;
    
    /**
     * Audio format (e.g., "audio/mpeg")
     */
    private String format;
    
    /**
     * Duration of audio in milliseconds
     */
    private Long duration;
    
    /**
     * Lesson ID this audio belongs to
     */
    private UUID lessonId;
    
    /**
     * Status of the request: "success" or "error"
     */
    private String status;
    
    /**
     * Error message if status is "error"
     */
    private String message;
    
    /**
     * Create success response
     */
    public static TTSResponseDto success(String audioBase64, String format, Long duration, UUID lessonId) {
        return TTSResponseDto.builder()
                .audioBase64(audioBase64)
                .format(format)
                .duration(duration)
                .lessonId(lessonId)
                .status("success")
                .build();
    }
    
    /**
     * Create error response
     */
    public static TTSResponseDto error(String message, UUID lessonId) {
        return TTSResponseDto.builder()
                .status("error")
                .message(message)
                .lessonId(lessonId)
                .build();
    }
}
