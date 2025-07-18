package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.Answer;
import com.vinaacademy.platform.feature.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, UUID> {
    /**
     * Find answers by question
     */
    List<Answer> findByQuestion(Question question);
    
    /**
     * Find answers by question ID
     */
    List<Answer> findByQuestionId(UUID questionId);
    
    /**
     * Find correct answers by question ID
     */
    List<Answer> findByQuestionIdAndIsCorrect(UUID questionId, boolean isCorrect);

    List<Answer> findByQuestionIdIn(List<UUID> questionIds);

    void deleteByQuestionId(UUID id);

    long countByQuestionIdAndIsCorrect(UUID id, boolean b);
}