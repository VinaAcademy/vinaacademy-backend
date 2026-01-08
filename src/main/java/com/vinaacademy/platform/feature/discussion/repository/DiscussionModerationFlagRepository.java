package com.vinaacademy.platform.feature.discussion.repository;

import com.vinaacademy.platform.feature.discussion.entity.DiscussionModerationFlag;
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
 * Repository for DiscussionModerationFlag entity
 */
@Repository
public interface DiscussionModerationFlagRepository extends JpaRepository<DiscussionModerationFlag, Long> {

    /**
     * Find flags by discussion ID
     */
    List<DiscussionModerationFlag> findByDiscussionId(UUID discussionId);

    /**
     * Find active (pending) flag for a discussion
     */
    Optional<DiscussionModerationFlag> findByDiscussionIdAndStatus(UUID discussionId, ModerationStatus status);

    /**
     * Find all flagged discussions by status
     */
    Page<DiscussionModerationFlag> findByStatus(ModerationStatus status, Pageable pageable);

    /**
     * Find all flagged discussions by status and severity
     */
    @Query("SELECT dmf FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.status = :status " +
            "AND dmf.severity >= :minSeverity " +
            "ORDER BY dmf.severity DESC, dmf.createdDate DESC")
    Page<DiscussionModerationFlag> findByStatusAndMinSeverity(
            @Param("status") ModerationStatus status,
            @Param("minSeverity") Integer minSeverity,
            Pageable pageable
    );

    /**
     * Find all pending flags sorted by severity
     */
    @Query("SELECT dmf FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.status = 'PENDING' " +
            "ORDER BY dmf.severity DESC, dmf.createdDate ASC")
    Page<DiscussionModerationFlag> findPendingOrderedBySeverity(Pageable pageable);

    /**
     * Find all processed flags (approved or rejected)
     */
    @Query("SELECT dmf FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.status IN ('APPROVED', 'REJECTED') " +
            "ORDER BY dmf.reviewedAt DESC")
    Page<DiscussionModerationFlag> findProcessedFlags(Pageable pageable);

    /**
     * Count flags by status
     */
    long countByStatus(ModerationStatus status);

    /**
     * Count flags by status and flag type
     */
    long countByStatusAndFlagType(ModerationStatus status, FlagType flagType);

    /**
     * Find flags created after specific date
     */
    @Query("SELECT dmf FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.createdDate >= :fromDate " +
            "ORDER BY dmf.createdDate DESC")
    List<DiscussionModerationFlag> findFlagsCreatedAfter(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Count critical pending flags (severity >= 4)
     */
    @Query("SELECT COUNT(dmf) FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.status = 'PENDING' AND dmf.severity >= 4")
    long countCriticalPendingFlags();

    /**
     * Check if discussion has any pending flag
     */
    boolean existsByDiscussionIdAndStatus(UUID discussionId, ModerationStatus status);
    
    /**
     * Check if discussion has any negative pending flag
     */
    @Query("SELECT CASE WHEN COUNT(dmf) > 0 THEN true ELSE false END FROM DiscussionModerationFlag dmf " +
            "WHERE dmf.discussion.id = :discussionId " +
            "AND dmf.status = 'PENDING' " +
            "AND dmf.flagType IN ('TOXIC', 'INAPPROPRIATE', 'EXTREME_NEGATIVE', 'ABUSIVE')")
    boolean hasNegativePendingFlag(@Param("discussionId") UUID discussionId);
}
