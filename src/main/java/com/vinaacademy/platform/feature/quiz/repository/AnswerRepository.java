package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
  /** Find answers by question */
  List<Answer> findByQuestion(Question question);

  /** Find answers by question ID */
  List<Answer> findByQuestionId(UUID questionId);

  /** Find correct answers by question ID */
  List<Answer> findByQuestionIdAndIsCorrect(UUID questionId, boolean isCorrect);

  List<Answer> findByQuestionIdIn(List<UUID> questionIds);

  @Modifying
  void deleteByQuestionId(UUID id);

  long countByQuestionIdAndIsCorrect(UUID id, boolean b);

  @Query(
      """
      SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END
      FROM Answer a
      JOIN a.userAnswers ua
      WHERE a.question.id = :questionId
  """)
  boolean existsByQuestionIdInUserAnswers(UUID questionId);
}
