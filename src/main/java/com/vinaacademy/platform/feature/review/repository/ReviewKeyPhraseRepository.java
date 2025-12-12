package com.vinaacademy.platform.feature.review.repository;

import com.vinaacademy.platform.feature.review.entity.ReviewKeyPhrase;
import com.vinaacademy.platform.feature.review.enums.AspectCategory;
import com.vinaacademy.platform.feature.review.enums.PhraseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ReviewKeyPhrase entity
 */
@Repository
public interface ReviewKeyPhraseRepository extends JpaRepository<ReviewKeyPhrase, Long> {
    
    /**
     * Find all key phrases for a review
     */
    List<ReviewKeyPhrase> findByReviewId(Long reviewId);
    
    /**
     * Find key phrases by type for a review
     */
    List<ReviewKeyPhrase> findByReviewIdAndPhraseType(Long reviewId, PhraseType phraseType);
    
    /**
     * Find all key phrases for a course
     */
    @Query("SELECT kp FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId")
    List<ReviewKeyPhrase> findByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find pros (positive phrases) for a course
     */
    @Query("SELECT kp FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.phraseType = 'PRO' " +
           "ORDER BY kp.confidence DESC")
    List<ReviewKeyPhrase> findProsByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find cons (negative phrases) for a course
     */
    @Query("SELECT kp FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.phraseType = 'CON' " +
           "ORDER BY kp.confidence DESC")
    List<ReviewKeyPhrase> findConsByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find top pros with high confidence and count
     */
    @Query("SELECT kp.phrase, COUNT(kp) as count " +
           "FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.phraseType = 'PRO' " +
           "AND kp.confidence >= 0.7 " +
           "GROUP BY kp.phrase " +
           "ORDER BY count DESC, MAX(kp.confidence) DESC")
    List<Object[]> findTopProsByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find top cons with high confidence and count
     */
    @Query("SELECT kp.phrase, COUNT(kp) as count " +
           "FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.phraseType = 'CON' " +
           "AND kp.confidence >= 0.7 " +
           "GROUP BY kp.phrase " +
           "ORDER BY count DESC, MAX(kp.confidence) DESC")
    List<Object[]> findTopConsByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find key phrases by category
     */
    @Query("SELECT kp FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.category = :category")
    List<ReviewKeyPhrase> findByCourseIdAndCategory(
        @Param("courseId") UUID courseId,
        @Param("category") AspectCategory category
    );
    
    /**
     * Count phrases by category for a course
     */
    @Query("SELECT kp.category, kp.phraseType, COUNT(kp) " +
           "FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "GROUP BY kp.category, kp.phraseType")
    List<Object[]> countByCategory(@Param("courseId") UUID courseId);
    
    /**
     * Find most mentioned aspects
     */
    @Query("SELECT kp.category, COUNT(kp) as count " +
           "FROM ReviewKeyPhrase kp " +
           "JOIN kp.review r " +
           "WHERE r.course.id = :courseId " +
           "AND kp.phraseType = 'ASPECT' " +
           "GROUP BY kp.category " +
           "ORDER BY count DESC")
    List<Object[]> findMostMentionedAspects(@Param("courseId") UUID courseId);
    
    /**
     * Delete key phrases for a review
     */
    void deleteByReviewId(Long reviewId);
}
