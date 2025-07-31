package com.vinaacademy.platform.feature.quiz.service.internal.impl;

import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import com.vinaacademy.platform.feature.quiz.repository.QuizSessionRepository;
import com.vinaacademy.platform.feature.quiz.service.internal.QuizSessionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class QuizSessionHandlerImpl implements QuizSessionHandler {

    private final QuizSessionRepository quizSessionRepository;
    private final TaskScheduler taskScheduler;

    public QuizSessionHandlerImpl(QuizSessionRepository quizSessionRepository,
                                  @Qualifier("quizScheduler") TaskScheduler taskScheduler) {
        this.quizSessionRepository = quizSessionRepository;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public Optional<QuizSession> findActiveSession(UUID quizId, UUID userId) {
        return quizSessionRepository.findFirstByQuizIdAndUserIdAndActiveTrue(quizId, userId);
    }

    @Override
    public void scheduleSessionExpiry(QuizSession session) {
        if (session.getExpiryTime() != null) {
            taskScheduler.schedule(() -> {
                log.info("Auto-expiring quiz session: {}", session.getId());
                deactivateSession(session);
            }, session.getExpiryTime()
                    .atZone(ZoneOffset.systemDefault())
                    .toInstant());
        }
    }

    @Override
    public void deactivateSession(QuizSession session) {
        session.setActive(false);
        quizSessionRepository.save(session);
    }
}
