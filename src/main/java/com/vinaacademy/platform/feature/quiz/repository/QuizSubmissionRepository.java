package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.QuizSubmission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

  @Query(
      """
    SELECT s FROM QuizSubmission s
        JOIN FETCH s.quizSession qs
        JOIN FETCH qs.user u
    WHERE qs.quiz.id = :quizId
    ORDER BY s.createdDate DESC
    """)
  List<QuizSubmission> findByQuizIdOrderByCreatedDateDesc(UUID quizId);
}