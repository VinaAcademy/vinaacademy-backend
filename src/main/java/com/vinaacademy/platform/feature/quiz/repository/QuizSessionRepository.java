package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {

    /**
     * Find the first active quiz session for a user and quiz
     * Using findFirst to ensure we get only one result even if multiple active sessions exist
     */
    Optional<QuizSession> findFirstByQuizIdAndUserIdAndActiveTrue(UUID quizId, UUID userId);

    /**
     * Find all active sessions for a quiz
     */
    List<QuizSession> findByQuizIdAndActiveTrue(UUID quizId);
    
    /**
     * Find all sessions for a user across all quizzes
     */
    List<QuizSession> findByUserId(UUID userId);
}