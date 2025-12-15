package com.vinaacademy.platform.feature.review.entity;

import com.vinaacademy.platform.feature.common.entity.BaseEntity;
import com.vinaacademy.platform.feature.review.enums.FlagType;
import com.vinaacademy.platform.feature.review.enums.ModerationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a moderation flag on a review
 * Used for auto-flagging toxic or inappropriate content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_moderation_flags", indexes = {
    @Index(name = "idx_pending_flags", columnList = "status, severity, created_date"),
    @Index(name = "idx_review_flags", columnList = "review_id, status"),
    @Index(name = "idx_flag_type", columnList = "flag_type, status")
})
public class ReviewModerationFlag extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CourseReview review;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", length = 50, nullable = false)
    private FlagType flagType;
    
    /**
     * Severity level from 1 (low) to 5 (critical)
     */
    @Column(name = "severity", nullable = false)
    private Integer severity;
    
    @Column(name = "confidence", precision = 5, scale = 4)
    private BigDecimal confidence;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    // Moderation Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private ModerationStatus status = ModerationStatus.PENDING;
    
    @Column(name = "reviewed_by")
    private UUID reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "moderator_notes", columnDefinition = "TEXT")
    private String moderatorNotes;
    
    /**
     * Helper method to check if flag is pending review
     */
    public boolean isPending() {
        return status == ModerationStatus.PENDING;
    }
    
    /**
     * Helper method to check if this is a critical flag
     */
    public boolean isCritical() {
        return severity >= 4;
    }
    
    /**
     * Helper method to approve the flag
     */
    public void approve(UUID moderatorId, String notes) {
        this.status = ModerationStatus.APPROVED;
        this.reviewedBy = moderatorId;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }
    
    /**
     * Helper method to reject the flag
     */
    public void reject(UUID moderatorId, String notes) {
        this.status = ModerationStatus.REJECTED;
        this.reviewedBy = moderatorId;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }
}
