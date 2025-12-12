package com.vinaacademy.platform.feature.review.repository;

import com.vinaacademy.platform.feature.review.entity.ReviewSentimentAnalysis;
import com.vinaacademy.platform.feature.review.enums.SentimentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ReviewSentimentAnalysis entity
 */
@Repository
public interface ReviewSentimentAnalysisRepository extends JpaRepository<ReviewSentimentAnalysis, Long> {
    
    /**
     * Find sentiment analysis by review ID
     */
    Optional<ReviewSentimentAnalysis> findByReviewId(Long reviewId);
    
    /**
     * Find all sentiment analyses for a course
     */
    @Query("SELECT rsa FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.deletedAt IS NULL")
    List<ReviewSentimentAnalysis> findByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Find sentiment analyses by course and sentiment type
     */
    @Query("SELECT rsa FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.sentiment = :sentiment " +
           "AND rsa.deletedAt IS NULL")
    List<ReviewSentimentAnalysis> findByCourseIdAndSentiment(
        @Param("courseId") UUID courseId,
        @Param("sentiment") SentimentType sentiment
    );
    
    /**
     * Find toxic reviews for a course
     */
    @Query("SELECT rsa FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.isToxic = true " +
           "AND rsa.deletedAt IS NULL")
    List<ReviewSentimentAnalysis> findToxicReviewsByCourseId(@Param("courseId") UUID courseId);
    
    /**
     * Count reviews by sentiment for a course
     */
    @Query("SELECT rsa.sentiment, COUNT(rsa) FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.deletedAt IS NULL " +
           "GROUP BY rsa.sentiment")
    List<Object[]> countBySentimentForCourse(@Param("courseId") UUID courseId);
    
    /**
     * Find sentiment analyses within date range
     */
    @Query("SELECT rsa FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.analyzedAt BETWEEN :startDate AND :endDate " +
           "AND rsa.deletedAt IS NULL " +
           "ORDER BY rsa.analyzedAt DESC")
    List<ReviewSentimentAnalysis> findByCourseIdAndDateRange(
        @Param("courseId") UUID courseId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Calculate average sentiment scores for a course
     */
    @Query("SELECT " +
           "AVG(rsa.confidencePositive), " +
           "AVG(rsa.confidenceNeutral), " +
           "AVG(rsa.confidenceNegative) " +
           "FROM ReviewSentimentAnalysis rsa " +
           "JOIN rsa.review r " +
           "WHERE r.course.id = :courseId " +
           "AND rsa.deletedAt IS NULL")
    Object[] calculateAverageScores(@Param("courseId") UUID courseId);
    
    /**
     * Check if review has sentiment analysis
     */
    boolean existsByReviewId(Long reviewId);
    
    /**
     * Delete sentiment analysis for a review
     */
    void deleteByReviewId(Long reviewId);
}
