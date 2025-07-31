package com.vinaacademy.platform.feature.quiz.service.student.internal.impl;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import com.vinaacademy.platform.feature.quiz.entity.UserAnswer;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.repository.QuestionRepository;
import com.vinaacademy.platform.feature.quiz.service.student.internal.QuizGradingService;
import com.vinaacademy.platform.feature.quiz.service.student.internal.strategy.GradingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizGradingServiceImpl implements QuizGradingService {

    private final List<GradingStrategy> gradingStrategies;
    private final QuestionRepository questionRepository;

    @Override
    public double calculateScore(Quiz quiz, QuizSubmission submission, List<UserAnswerRequest> answers) {
        double earnedPoints = 0;

        for (UserAnswerRequest answerRequest : answers) {
            Question question = questionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new ValidationException("Question not found: " + answerRequest.getQuestionId()));

            UserAnswer userAnswer = gradeAnswer(question, answerRequest);
            userAnswer.setSubmission(submission);
            submission.addUserAnswer(userAnswer);

            if (userAnswer.isCorrect() || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                earnedPoints += userAnswer.getEarnedPoints();
            }
        }

        return Double.parseDouble(new DecimalFormat("#.0").format(earnedPoints));
    }

    @Override
    public UserAnswer gradeAnswer(Question question, UserAnswerRequest request) {
        GradingStrategy strategy = gradingStrategies.stream()
                .filter(s -> s.supports(question.getQuestionType()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("No grading strategy found for question type: " + question.getQuestionType()));

        return strategy.grade(question, request);
    }
}
