package com.vinaacademy.platform.feature.review.repository;

import com.vinaacademy.platform.feature.review.entity.ReviewModerationFlag;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ReviewModerationFlag entity
 */
@Repository
public interface ReviewModerationFlagRepository extends JpaRepository<ReviewModerationFlag, Long> {

    /**
     * Find flags by review ID
     */
    List<ReviewModerationFlag> findByReviewId(Long reviewId);

    /**
     * Find active (pending) flag for a review
     */
    Optional<ReviewModerationFlag> findByReviewIdAndStatus(Long reviewId, ModerationStatus status);

    /**
     * Find all flagged reviews by status
     */
    Page<ReviewModerationFlag> findByStatus(ModerationStatus status, Pageable pageable);

    /**
     * Find all flagged reviews by status and severity
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.status = :status " +
            "AND rmf.severity >= :minSeverity " +
            "ORDER BY rmf.severity DESC, rmf.createdDate DESC")
    Page<ReviewModerationFlag> findByStatusAndMinSeverity(
            @Param("status") ModerationStatus status,
            @Param("minSeverity") Integer minSeverity,
            Pageable pageable
    );

    /**
     * Find pending flags (for admin moderation queue)
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.status = 'PENDING' " +
            "ORDER BY rmf.severity DESC, rmf.createdDate ASC")
    Page<ReviewModerationFlag> findPendingFlags(Pageable pageable);

    /**
     * Find processed (non-pending) flags - for moderation history
     * Includes: APPROVED, REJECTED, AUTO_APPROVED
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.status != 'PENDING' " +
            "ORDER BY rmf.reviewedAt DESC, rmf.createdDate DESC")
    Page<ReviewModerationFlag> findProcessedFlags(Pageable pageable);

    /**
     * Find all flags (for moderation queue - ALL filter)
     * Sắp xếp: PENDING lên trước, theo severity giảm dần
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "ORDER BY CASE WHEN rmf.status = 'PENDING' THEN 0 ELSE 1 END, " +
            "rmf.severity DESC, rmf.createdDate DESC")
    Page<ReviewModerationFlag> findAllFlagsOrderedForQueue(Pageable pageable);

    /**
     * Find critical pending flags (severity >= 4)
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.status = 'PENDING' " +
            "AND rmf.severity >= 4 " +
            "ORDER BY rmf.severity DESC, rmf.createdDate ASC")
    List<ReviewModerationFlag> findCriticalPendingFlags();

    /**
     * Find flags by course
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "JOIN rmf.review r " +
            "WHERE r.course.id = :courseId")
    List<ReviewModerationFlag> findByCourseId(@Param("courseId") UUID courseId);

    /**
     * Find flags by type
     */
    List<ReviewModerationFlag> findByFlagType(FlagType flagType);

    /**
     * Count pending flags
     */
    long countByStatus(ModerationStatus status);

    /**
     * Count pending flags by course
     */
    @Query("SELECT COUNT(rmf) FROM ReviewModerationFlag rmf " +
            "JOIN rmf.review r " +
            "WHERE r.course.id = :courseId " +
            "AND rmf.status = 'PENDING'")
    long countPendingFlagsByCourseId(@Param("courseId") UUID courseId);

    /**
     * Find flags reviewed by a moderator
     */
    List<ReviewModerationFlag> findByReviewedBy(UUID moderatorId);

    /**
     * Find flags within date range
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.createdDate BETWEEN :startDate AND :endDate " +
            "ORDER BY rmf.createdDate DESC")
    List<ReviewModerationFlag> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Check if review has pending flags
     */
    boolean existsByReviewIdAndStatus(Long reviewId, ModerationStatus status);

    /**
     * Find toxic content flags
     */
    @Query("SELECT rmf FROM ReviewModerationFlag rmf " +
            "WHERE rmf.flagType = 'TOXIC' " +
            "AND rmf.status = 'PENDING' " +
            "ORDER BY rmf.severity DESC, rmf.createdDate ASC")
    Page<ReviewModerationFlag> findToxicContentFlags(Pageable pageable);

    /**
     * Get moderation statistics
     */
    @Query("SELECT rmf.status, COUNT(rmf) " +
            "FROM ReviewModerationFlag rmf " +
            "GROUP BY rmf.status")
    List<Object[]> getModerationStatistics();

    /**
     * Count critical pending flags (severity >= 4, status = PENDING)
     */
    @Query("SELECT COUNT(rmf) FROM ReviewModerationFlag rmf " +
            "WHERE rmf.status = 'PENDING' AND rmf.severity >= 4")
    long countCriticalPending();
}
