package com.vinaacademy.platform.feature.lesson.service;

import com.vinaacademy.platform.feature.lesson.dto.TTSRequestDto;
import com.vinaacademy.platform.feature.lesson.dto.TTSResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

/**
 * Service interface for Text-to-Speech functionality for Reading lessons
 */
public interface LessonTTSService {
    
    /**
     * Generate audio for a Reading lesson
     * 
     * @param lessonId ID of the Reading lesson
     * @param request TTS configuration (voice, speed, etc.)
     * @return TTSResponseDto containing audio data or error message
     */
    TTSResponseDto generateAudioForLesson(UUID lessonId, TTSRequestDto request);
    
    /**
     * Stream audio for a Reading lesson
     * Useful for large content to avoid memory issues
     * 
     * @param lessonId ID of the Reading lesson
     * @param voice Voice to use for synthesis
     * @return ResponseEntity with streaming audio
     */
    ResponseEntity<StreamingResponseBody> streamAudioForLesson(UUID lessonId, String voice);
}
