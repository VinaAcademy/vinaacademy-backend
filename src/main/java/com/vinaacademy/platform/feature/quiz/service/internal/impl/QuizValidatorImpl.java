package com.vinaacademy.platform.feature.quiz.service.internal.impl;

import com.vinaacademy.platform.exception.ValidationException;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import com.vinaacademy.platform.feature.quiz.repository.QuizSessionRepository;
import com.vinaacademy.platform.feature.quiz.repository.QuizSubmissionRepository;
import com.vinaacademy.platform.feature.quiz.service.internal.QuizValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizValidatorImpl implements QuizValidator {

    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizSessionRepository quizSessionRepository;

    @Override
    public void validateRetakePolicy(Quiz quiz, UUID userId) {
        if (!quiz.isAllowRetake()) {
            boolean hasSubmission = quizSubmissionRepository
                    .findFirstByQuizIdAndUserIdOrderByCreatedDateDesc(quiz.getId(), userId)
                    .isPresent();

            if (hasSubmission) {
                throw new ValidationException("Retaking this quiz is not allowed");
            }
        }
    }

    @Override
    public void validateTimeLimit(Quiz quiz, QuizSession session, LocalDateTime start, LocalDateTime end) {
        if (session.isSubmitted()) {
            throw new ValidationException("Quiz has already been submitted.");
        }
        if (quiz.getTimeLimit() > 0) {
            long durationInMinutes = java.time.Duration.between(start, end).toMinutes();

            if (durationInMinutes > quiz.getTimeLimit()) {
                session.setActive(false);
                quizSessionRepository.save(session);
                throw new ValidationException("Time limit exceeded. Quiz time limit is " +
                        quiz.getTimeLimit() + " minutes, but you took " + durationInMinutes + " minutes");
            }
        }
    }

    @Override
    public QuizSession validateActiveSession(UUID quizId, UUID userId) {
        Optional<QuizSession> activeSession = quizSessionRepository
                .findFirstByQuizIdAndUserIdAndActiveTrue(quizId, userId);

        if (activeSession.isEmpty()) {
            throw new ValidationException("No active quiz session found. Please start the quiz before submitting.");
        }

        return activeSession.get();
    }
}
