package com.vinaacademy.platform.feature.quiz.service.impl;

import com.vinaacademy.platform.feature.quiz.dto.QuizSubmissionRequest;
import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import com.vinaacademy.platform.feature.quiz.repository.QuizSessionRepository;
import com.vinaacademy.platform.feature.quiz.service.QuizCacheService;
import com.vinaacademy.platform.feature.quiz.service.QuizSessionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizSessionServiceImpl implements QuizSessionService {
    private final QuizSessionRepository quizSessionRepository;
    private final QuizCacheService quizCacheService;


    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Override
    public void deactivateSession(QuizSession session) {
        entityManager.lock(session, LockModeType.PESSIMISTIC_WRITE);
        session.setActive(false);
        quizSessionRepository.save(session);
        // remove cached answers
        quizCacheService.clearCache(session.getUser().getId(), session.getId(), session.getQuiz().getId());
    }

    @Override
    public QuizSubmissionRequest getQuizSubmissionBySession(QuizSession session) {
        // submit cached answers to the database
        List<UserAnswerRequest> cachedAnswers = quizCacheService.getCachedUserAnswers(session.getUser().getId(), session.getId(), session.getQuiz().getId())
                .values()
                .stream()
                .toList();
        if (cachedAnswers.isEmpty()) {
            return QuizSubmissionRequest.builder()
                    .quizId(session.getQuiz().getId())
                    .answers(List.of())
                    .build();
        }
        return QuizSubmissionRequest.builder()
                .quizId(session.getQuiz().getId())
                .answers(cachedAnswers)
                .build();
    }
}
