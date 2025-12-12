package com.vinaacademy.platform.feature.review.repository;

import com.vinaacademy.platform.feature.review.entity.CourseSentimentStatistics;
import com.vinaacademy.platform.feature.review.entity.CourseSentimentStatistics.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CourseSentimentStatistics entity
 */
@Repository
public interface CourseSentimentStatisticsRepository extends JpaRepository<CourseSentimentStatistics, Long> {
    
    /**
     * Find statistics by course and period
     */
    Optional<CourseSentimentStatistics> findByCourseIdAndPeriodTypeAndPeriodStart(
        UUID courseId,
        PeriodType periodType,
        LocalDate periodStart
    );
    
    /**
     * Find all statistics for a course by period type
     */
    @Query("SELECT css FROM CourseSentimentStatistics css " +
           "WHERE css.course.id = :courseId " +
           "AND css.periodType = :periodType " +
           "ORDER BY css.periodStart DESC")
    List<CourseSentimentStatistics> findByCourseIdAndPeriodType(
        @Param("courseId") UUID courseId,
        @Param("periodType") PeriodType periodType
    );
    
    /**
     * Find statistics within date range
     */
    @Query("SELECT css FROM CourseSentimentStatistics css " +
           "WHERE css.course.id = :courseId " +
           "AND css.periodType = :periodType " +
           "AND css.periodStart >= :startDate " +
           "AND css.periodEnd <= :endDate " +
           "ORDER BY css.periodStart ASC")
    List<CourseSentimentStatistics> findByCourseIdAndDateRange(
        @Param("courseId") UUID courseId,
        @Param("periodType") PeriodType periodType,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Find latest statistics for a course
     */
    @Query("SELECT css FROM CourseSentimentStatistics css " +
           "WHERE css.course.id = :courseId " +
           "AND css.periodType = :periodType " +
           "ORDER BY css.periodStart DESC " +
           "LIMIT 1")
    Optional<CourseSentimentStatistics> findLatestByCourseIdAndPeriodType(
        @Param("courseId") UUID courseId,
        @Param("periodType") PeriodType periodType
    );
    
    /**
     * Find statistics for multiple courses
     */
    @Query("SELECT css FROM CourseSentimentStatistics css " +
           "WHERE css.course.id IN :courseIds " +
           "AND css.periodType = :periodType " +
           "ORDER BY css.course.id, css.periodStart DESC")
    List<CourseSentimentStatistics> findByMultipleCourses(
        @Param("courseIds") List<UUID> courseIds,
        @Param("periodType") PeriodType periodType
    );
    
    /**
     * Get trend data for dashboard (last N periods)
     */
    @Query("SELECT css FROM CourseSentimentStatistics css " +
           "WHERE css.course.id = :courseId " +
           "AND css.periodType = :periodType " +
           "ORDER BY css.periodStart DESC " +
           "LIMIT :limit")
    List<CourseSentimentStatistics> findRecentTrend(
        @Param("courseId") UUID courseId,
        @Param("periodType") PeriodType periodType,
        @Param("limit") int limit
    );
    
    /**
     * Delete old statistics before a certain date (for cleanup)
     */
    @Query("DELETE FROM CourseSentimentStatistics css " +
           "WHERE css.periodEnd < :beforeDate")
    void deleteOldStatistics(@Param("beforeDate") LocalDate beforeDate);
    
    /**
     * Check if statistics exist for a period
     */
    boolean existsByCourseIdAndPeriodTypeAndPeriodStart(
        UUID courseId,
        PeriodType periodType,
        LocalDate periodStart
    );
}
