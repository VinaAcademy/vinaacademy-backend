package com.vinaacademy.platform.feature.review.enums;

/**
 * Enum representing the sentiment type of a review
 * Based on Azure Text Analytics sentiment analysis results
 */
public enum SentimentType {
    /**
     * Review has predominantly positive sentiment
     */
    POSITIVE("Tích cực"),
    
    /**
     * Review has predominantly negative sentiment
     */
    NEGATIVE("Tiêu cực"),
    
    /**
     * Review has neutral sentiment
     */
    NEUTRAL("Trung lập"),
    
    /**
     * Review contains both positive and negative sentiments
     */
    MIXED("Hỗn hợp");

    private final String displayName;

    SentimentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
