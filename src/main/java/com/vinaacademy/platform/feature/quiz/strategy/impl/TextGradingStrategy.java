package com.vinaacademy.platform.feature.quiz.strategy.impl;

import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.strategy.GradingStrategy;
import org.springframework.stereotype.Component;

@Component
public class TextGradingStrategy implements GradingStrategy {

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.TEXT;
    }

    @Override
    public UserAnswer grade(Question question, UserAnswerRequest request) {
        UserAnswer userAnswer = UserAnswer.builder()
                .question(question)
                .textAnswer(request.getTextAnswer())
                .build();

        // Text-based answers need manual grading by instructors
        userAnswer.setCorrect(false); // Not automatically graded
        userAnswer.setEarnedPoints(0); // Will be updated by instructor

        return userAnswer;
    }
}
