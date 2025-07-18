package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.Question;
import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    /**
     * Find questions by quiz
     */
    List<Question> findByQuizOrderByCreatedDate(Quiz quiz);
    
    /**
     * Find questions by quiz ID
     */
    List<Question> findByQuizId(UUID quizId);
}