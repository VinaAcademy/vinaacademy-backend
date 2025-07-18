package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, UUID> {
    /**
     * Find submissions for a specific quiz by a user
     */
    @Query("SELECT submission FROM QuizSubmission submission " +
            "JOIN submission.quizSession session " +
            "WHERE session.quiz.id = :quizId AND session.user.id = :userId "
            + "ORDER BY submission.createdDate DESC")
    List<QuizSubmission> findByQuizIdAndUserIdOrderByCreatedDateDesc(UUID quizId, UUID userId);

    /**
     * Find the latest submission for a specific quiz by a user
     */
    @Query("SELECT submission FROM QuizSubmission submission " +
            "JOIN submission.quizSession session " +
            "WHERE session.quiz.id = :quizId AND session.user.id = :userId " +
            "ORDER BY submission.createdDate DESC " +
            "LIMIT 1")
    Optional<QuizSubmission> findFirstByQuizIdAndUserIdOrderByCreatedDateDesc(UUID quizId, UUID userId);
}