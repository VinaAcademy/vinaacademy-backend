package com.vinaacademy.platform.feature.quiz.service.internal;

import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;

import java.util.List;

public interface QuizGradingService {
    double calculateScore(Quiz quiz, QuizSubmission submission, List<UserAnswerRequest> answers);
    UserAnswer gradeAnswer(Question question, UserAnswerRequest request);
}
