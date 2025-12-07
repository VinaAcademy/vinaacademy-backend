package com.vinaacademy.platform.feature.lesson.service.impl;

import com.vinaacademy.platform.exception.NotFoundException;
import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.common.exception.TTSException;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.enrollment.repository.EnrollmentRepository;
import com.vinaacademy.platform.feature.lesson.dto.*;
import com.vinaacademy.platform.feature.lesson.entity.Lesson;
import com.vinaacademy.platform.feature.lesson.repository.LessonRepository;
import com.vinaacademy.platform.feature.lesson.service.LessonTTSService;
import com.vinaacademy.platform.feature.reading.Reading;
import com.vinaacademy.platform.feature.user.auth.helpers.SecurityHelper;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Implementation of LessonTTSService for Text-to-Speech functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LessonTTSServiceImpl implements LessonTTSService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final WebClient.Builder webClientBuilder; // Non-load-balanced for direct URLs
    private final WebClient.Builder loadBalancedWebClientBuilder; // Load-balanced for service discovery
    private final SecurityHelper securityHelper;

    @Value("${tts.service.url:lb://TEXT-TO-SPEECH-SERVICE}")
    private String ttsServiceUrl;

    @Value("${tts.max-text-length:5000}")
    private int maxTextLength;

    @Value("${tts.timeout:30000}")
    private long ttsTimeout;

    /**
     * Selects the appropriate WebClient.Builder based on URL pattern
     * @return WebClient.Builder - load-balanced for lb:// URLs, direct for http/https URLs
     */
    private WebClient.Builder getWebClientBuilder() {
        if (ttsServiceUrl.startsWith("lb://")) {
            log.debug("Using load-balanced WebClient for service discovery: {}", ttsServiceUrl);
            return loadBalancedWebClientBuilder;
        } else {
            log.debug("Using direct WebClient for URL: {}", ttsServiceUrl);
            return webClientBuilder;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TTSResponseDto generateAudioForLesson(UUID lessonId, TTSRequestDto request) {
        log.info("Generating TTS audio for lesson: {}", lessonId);

        try {
            // 1. Validate lesson exists and is Reading type
            Reading reading = validateAndGetReading(lessonId);

            // 2. Check user has access permission
            validateUserAccess(reading);

            // 3. Extract and sanitize content
            String cleanText = sanitizeHtmlContent(reading.getContent());

            if (cleanText == null || cleanText.trim().isEmpty()) {
                throw new ValidationException("Nội dung bài học trống, không thể tạo audio");
            }

            if (cleanText.length() > maxTextLength) {
                throw TTSException.contentTooLong(maxTextLength);
            }

            log.debug("Sanitized text length: {} characters", cleanText.length());

            // 4. Build TTS service request
            TTSServiceRequest ttsRequest = buildTTSRequest(reading, cleanText, request);

            // 5. Call TTS Service via WebClient
            TTSServiceResponse ttsResponse = callTTSService(ttsRequest);

            // 6. Validate response
            if (ttsResponse == null || !"success".equals(ttsResponse.getStatus())) {
                String errorMsg = ttsResponse != null ? ttsResponse.getMessage() : "Unknown error";
                log.error("TTS service returned error: {}", errorMsg);
                throw TTSException.audioGenerationFailed();
            }

            // 7. Build and return response
            return TTSResponseDto.success(
                    ttsResponse.getAudioBase64(),
                    ttsResponse.getFormat(),
                    ttsResponse.getDuration(),
                    lessonId
            );

        } catch (TTSException | ValidationException e) {
            log.error("TTS validation error for lesson {}: {}", lessonId, e.getMessage());
            return TTSResponseDto.error(e.getMessage(), lessonId);
        } catch (Exception e) {
            log.error("Unexpected error generating TTS for lesson {}", lessonId, e);
            return TTSResponseDto.error(
                    "Lỗi hệ thống khi tạo audio: " + e.getMessage(),
                    lessonId
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<StreamingResponseBody> streamAudioForLesson(UUID lessonId, String voice) {
        log.info("Streaming TTS audio for lesson: {}", lessonId);

        try {
            // 1. Validate lesson
            Reading reading = validateAndGetReading(lessonId);

            // 2. Check access
            validateUserAccess(reading);

            // 3. Sanitize content
            String cleanText = sanitizeHtmlContent(reading.getContent());

            if (cleanText == null || cleanText.trim().isEmpty()) {
                throw new ValidationException("Nội dung bài học trống");
            }

            if (cleanText.length() > maxTextLength) {
                throw TTSException.contentTooLong(maxTextLength);
            }

            // 4. Build request
            TTSServiceRequest ttsRequest = TTSServiceRequest.builder()
                    .text(cleanText)
                    .voice(voice != null ? voice : "vi-VN-HoaiMyNeural")
                    .language("vi-VN")
                    .speed("1.0")
                    .pitch("0Hz")
                    .lessonId(lessonId.toString())
                    .courseId(reading.getSection().getCourse().getId().toString())
                    .build();

            // 5. Create streaming response
            StreamingResponseBody stream = outputStream -> {
                try {
                    getWebClientBuilder().build()
                            .post()
                            .uri(ttsServiceUrl + "/api/v1/tts/synthesize-stream")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ttsRequest)
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .timeout(Duration.ofMillis(ttsTimeout))
                            .doOnError(error -> log.error("Error streaming audio", error))
                            .subscribe(
                                    audioData -> {
                                        try {
                                            outputStream.write(audioData);
                                            outputStream.flush();
                                        } catch (Exception e) {
                                            log.error("Error writing audio stream", e);
                                        }
                                    },
                                    error -> log.error("Stream subscription error", error)
                            );
                } catch (Exception e) {
                    log.error("Error in streaming response", e);
                    throw new RuntimeException(e);
                }
            };

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "audio/mpeg");
            headers.add(HttpHeaders.CONTENT_DISPOSITION,
                    String.format("inline; filename=lesson-%s-audio.mp3", lessonId));

            return new ResponseEntity<>(stream, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error streaming audio for lesson {}", lessonId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Validate lesson exists and is of Reading type
     */
    private Reading validateAndGetReading(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài học với ID: " + lessonId));

        if (!(lesson instanceof Reading)) {
            throw TTSException.invalidLessonType();
        }

        return (Reading) lesson;
    }

    /**
     * Validate user has access to the lesson's course
     */
    private void validateUserAccess(Reading reading) {
        User currentUser = securityHelper.getCurrentUser();
        UUID courseId = reading.getSection().getCourse().getId();

        // Check if lesson is free
        if (reading.isFree()) {
            log.debug("Lesson is free, access granted");
            return;
        }

        // Check if user is enrolled
        boolean isEnrolled = enrollmentRepository
                .findByUserIdAndCourseId(currentUser.getId(), courseId)
                .isPresent();

        if (!isEnrolled) {
            throw new ValidationException(
                    "Bạn cần đăng ký khóa học để sử dụng tính năng Text-to-Speech");
        }
    }

    /**
     * Sanitize HTML content to plain text
     * Removes all HTML tags, scripts, styles and converts entities
     */
    private String sanitizeHtmlContent(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }

        // Remove all HTML tags and get plain text
        String plainText = Jsoup.clean(htmlContent, Safelist.none());

        // Decode HTML entities and normalize whitespace
        plainText = Jsoup.parse(plainText).text();

        // Additional cleanup
        plainText = plainText
                .replaceAll("\\s+", " ")  // Multiple spaces to single space
                .replaceAll("\\n+", " ")   // Multiple newlines to single space
                .trim();

        return plainText;
    }

    /**
     * Build TTS service request
     */
    private TTSServiceRequest buildTTSRequest(Reading reading, String text, TTSRequestDto request) {
        return TTSServiceRequest.builder()
                .text(text)
                .voice(request.getVoice())
                .language("vi-VN")
                .speed(request.getSpeed())
                .pitch(request.getPitch())
                .lessonId(reading.getId().toString())
                .sectionId(reading.getSection().getId().toString())
                .courseId(reading.getSection().getCourse().getId().toString())
                .build();
    }

    /**
     * Call TTS service via WebClient
     */
    private TTSServiceResponse callTTSService(TTSServiceRequest request) {
        try {
            log.debug("Calling TTS service at: {}", ttsServiceUrl);

            return getWebClientBuilder().build()
                    .post()
                    .uri(ttsServiceUrl + "/api/v1/tts/synthesize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TTSServiceResponse.class)
                    .timeout(Duration.ofMillis(ttsTimeout))
                    .doOnError(WebClientResponseException.class, error -> {
                        log.error("TTS service error - Status: {}, Body: {}",
                                error.getStatusCode(), error.getResponseBodyAsString());
                    })
                    .onErrorResume(WebClientResponseException.class, error -> {
                        log.error("TTS service communication error", error);
                        return Mono.error(TTSException.serviceUnavailable());
                    })
                    .block();

        } catch (Exception e) {
            log.error("Error calling TTS service", e);
            throw TTSException.serviceUnavailable();
        }
    }
}
