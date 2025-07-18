package com.vinaacademy.platform.feature.quiz.service;

import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionRequest;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;

public interface QuizSessionService {
    void deactivateSession(com.vinaacademy.platform.feature.quiz.entity.QuizSession quizSession);

    QuizSubmissionRequest getQuizSubmissionBySession(QuizSession session);
}
