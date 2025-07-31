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

import java.text.DecimalFormat;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MultipleChoiceGradingStrategy implements GradingStrategy {

    private final AnswerRepository answerRepository;

    @Override
    public boolean supports(QuestionType type) {
        return type == QuestionType.MULTIPLE_CHOICE;
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

            // Calculate partial scoring for multiple choice
            long correctCount = selectedAnswers.stream()
                    .filter(Answer::getIsCorrect)
                    .count();
            long incorrectCount = selectedAnswers.size() - correctCount;
            long totalCorrectCount = answerRepository
                    .countByQuestionIdAndIsCorrect(question.getId(), true);
            long totalIncorrectCount = answerRepository
                    .countByQuestionIdAndIsCorrect(question.getId(), false);

            if (totalCorrectCount == 0 || totalIncorrectCount == 0) {
                userAnswer.setEarnedPoints(0);
                userAnswer.setCorrect(false);
                return userAnswer;
            }

            double earnedPointRate = (correctCount * 1.0 / totalCorrectCount)
                    - (incorrectCount * 1.0 / totalIncorrectCount);

            if (earnedPointRate < 0) {
                earnedPointRate = 0;
            }

            double earnedPoints = Double.parseDouble(new DecimalFormat("#.0")
                    .format(earnedPointRate * question.getPoint()));

            userAnswer.setEarnedPoints(earnedPoints);

            // Check if it's fully correct (all correct answers selected, no incorrect ones)
            List<Answer> correctAnswers = answerRepository.findByQuestionIdAndIsCorrect(question.getId(), true);

            Set<Answer> selectedAnswerSet = new HashSet<>(selectedAnswers);
            boolean hasNoDuplicates = selectedAnswerSet.size() == selectedAnswers.size();

            boolean allSelectedAreCorrect = selectedAnswers.stream().allMatch(Answer::getIsCorrect);
            boolean allCorrectAreSelected = selectedAnswerSet.containsAll(correctAnswers) &&
                    correctAnswers.size() == selectedAnswerSet.size();

            boolean isFullyCorrect = hasNoDuplicates && allSelectedAreCorrect && allCorrectAreSelected;

            userAnswer.setCorrect(isFullyCorrect);
        } else {
            userAnswer.setCorrect(false);
            userAnswer.setEarnedPoints(0);
        }

        return userAnswer;
    }
}
