package com.vinaacademy.platform.feature.quiz.strategy.impl;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.repository.AnswerRepository;
import com.vinaacademy.platform.feature.quiz.strategy.GradingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SingleChoiceGradingStrategy implements GradingStrategy {

    private final AnswerRepository answerRepository;

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE;
    }

    @Override
    public UserAnswer grade(Question question, UserAnswerRequest request) {
        UserAnswer userAnswer = UserAnswer.builder()
                .question(question)
                .textAnswer(request.getTextAnswer())
                .build();

        if (request.getSelectedAnswerIds() != null && !request.getSelectedAnswerIds().isEmpty()) {
            List<Answer> selectedAnswers = new ArrayList<>();

            for (UUID answerId : request.getSelectedAnswerIds()) {
                Answer answer = answerRepository.findById(answerId)
                        .orElseThrow(() -> new ValidationException("Answer not found: " + answerId));
                selectedAnswers.add(answer);
            }

            userAnswer.setSelectedAnswers(selectedAnswers);

            // Single choice: exactly one answer should be selected and it should be correct
            boolean isCorrect = selectedAnswers.size() == 1 && selectedAnswers.get(0).getIsCorrect();
            userAnswer.setCorrect(isCorrect);
            userAnswer.setEarnedPoints(isCorrect ? question.getPoint() : 0);
        } else {
            userAnswer.setCorrect(false);
            userAnswer.setEarnedPoints(0);
        }

        return userAnswer;
    }
}
