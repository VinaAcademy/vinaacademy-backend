package com.vinaacademy.platform.feature.quiz.service.internal;

import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;

import java.time.LocalDateTime;
import java.util.UUID;

public interface QuizValidator {
    void validateRetakePolicy(Quiz quiz, UUID userId);
    void validateTimeLimit(Quiz quiz, QuizSession session, LocalDateTime start, LocalDateTime end);
    QuizSession validateActiveSession(UUID quizId, UUID userId);
}
