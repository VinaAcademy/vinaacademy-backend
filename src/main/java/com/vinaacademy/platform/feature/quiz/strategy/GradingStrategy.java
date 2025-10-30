package com.vinaacademy.platform.feature.quiz.strategy;

import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;

public interface GradingStrategy {
    boolean supports(QuestionType type);
    UserAnswer grade(Question question, UserAnswerRequest request);
}
