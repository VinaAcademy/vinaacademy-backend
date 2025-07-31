package com.vinaacademy.platform.feature.quiz.service.student.internal;

import com.vinaacademy.platform.feature.quiz.entity.QuizSession;

import java.util.Optional;
import java.util.UUID;

public interface QuizSessionHandler {
    Optional<QuizSession> findActiveSession(UUID quizId, UUID userId);

    void scheduleSessionExpiry(QuizSession session);

    void deactivateSession(QuizSession session);
}
