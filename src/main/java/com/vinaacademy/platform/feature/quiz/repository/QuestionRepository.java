package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
  /** Find questions by quiz */
  List<Question> findByQuizOrderByCreatedDate(Quiz quiz);

  /** Find questions by quiz ID */
  List<Question> findByQuizId(UUID quizId);

  @Query(
"""
      SELECT q FROM Question q
      LEFT JOIN FETCH q.answers a
      WHERE q.id = :questionId
""")
  Optional<Question> findByIdWithAnswer(UUID questionId);
}
