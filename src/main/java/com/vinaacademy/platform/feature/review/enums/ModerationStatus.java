package com.vinaacademy.platform.feature.review.enums;

/**
 * Enum representing the moderation status of a flagged review
 */
public enum ModerationStatus {
    /**
     * Flag is pending review by moderator
     */
    PENDING("Chờ xử lý"),
    
    /**
     * Flag has been reviewed but no action taken yet
     */
    REVIEWED("Đã xem xét"),
    
    /**
     * Flag was approved and action was taken (e.g., review hidden/deleted)
     */
    APPROVED("Đã chấp nhận"),
    
    /**
     * Flag was rejected, review is fine
     */
    REJECTED("Đã từ chối"),
    
    /**
     * Review was auto-approved without manual review
     */
    AUTO_APPROVED("Tự động chấp nhận");

    private final String displayName;

    ModerationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
