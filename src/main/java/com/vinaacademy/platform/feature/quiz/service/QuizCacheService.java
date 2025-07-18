package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface QuizCacheService {
    void cacheUserAnswers(UUID userId, UUID quizId, List<UserAnswerRequest> answers, UUID sessionId);
    Map<String, UserAnswerRequest> getCachedUserAnswers(UUID userId, UUID sessionId, UUID quizId);
    void updateCacheAnswer(UUID userId, UUID sessionId, UUID quizId, UserAnswerRequest request);
    void clearCache(UUID userId, UUID sessionId, UUID quizId);
}
