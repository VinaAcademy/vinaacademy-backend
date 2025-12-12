package com.vinaacademy.platform.feature.review.enums;

/**
 * Enum representing the type of moderation flag for a review
 */
public enum FlagType {
    /**
     * Review contains toxic or offensive content
     */
    TOXIC("Nội dung độc hại"),
    
    /**
     * Review appears to be spam
     */
    SPAM("Spam"),
    
    /**
     * Review contains inappropriate content
     */
    INAPPROPRIATE("Không phù hợp"),
    
    /**
     * Review has extremely negative sentiment that may need attention
     */
    EXTREME_NEGATIVE("Cực kỳ tiêu cực"),
    
    /**
     * Review may contain abusive language
     */
    ABUSIVE("Lạm dụng ngôn từ"),
    
    /**
     * Review flagged for other reasons
     */
    OTHER("Khác");

    private final String displayName;

    FlagType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
