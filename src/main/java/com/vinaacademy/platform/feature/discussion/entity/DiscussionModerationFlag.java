package com.vinaacademy.platform.feature.discussion.entity;

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
 * Entity representing a moderation flag on a discussion
 * Used for auto-flagging toxic or inappropriate content
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "discussion_moderation_flags", indexes = {
    @Index(name = "idx_discussion_pending_flags", columnList = "status, severity, created_date"),
    @Index(name = "idx_discussion_flags", columnList = "discussion_id, status"),
    @Index(name = "idx_discussion_flag_type", columnList = "flag_type, status")
})
public class DiscussionModerationFlag extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discussion_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Discussion discussion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", length = 50, nullable = false)
    private FlagType flagType;
    
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
        return severity >= 7;
    }
    
    /**
     * Helper method to approve the flag
     */
    public void approve(String notes) {
        this.status = ModerationStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }
    
    /**
     * Helper method to reject the flag
     */
    public void reject(String notes) {
        this.status = ModerationStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
        this.moderatorNotes = notes;
    }
}
