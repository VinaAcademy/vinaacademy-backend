package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizSessionRepository extends JpaRepository<QuizSession, UUID> {
    
    Optional<QuizSession> findByQuizIdAndUserIdAndActiveTrue(UUID quizId, UUID userId);
    
    void deleteByQuizIdAndUserId(UUID quizId, UUID userId);
}