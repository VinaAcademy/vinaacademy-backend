package com.vinaacademy.platform.feature.quiz.repository;

import com.vinaacademy.platform.feature.quiz.entity.Quiz;
import com.vinaacademy.platform.feature.section.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    
    /**
     * Find quizzes by course ID
     */
    @Query("SELECT q FROM Quiz q JOIN q.section s WHERE s.course.id = :courseId ORDER BY s.orderIndex, q.orderIndex")
    List<Quiz> findByCourseId(UUID courseId);
    
    /**
     * Find quizzes by section ID
     */
    List<Quiz> findBySectionIdOrderByOrderIndex(UUID sectionId);
    
    /**
     * Find quizzes by section ordered by their index
     */
    List<Quiz> findBySectionOrderByOrderIndex(Section section);
}
