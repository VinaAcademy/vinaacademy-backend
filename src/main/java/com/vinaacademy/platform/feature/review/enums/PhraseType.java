package com.vinaacademy.platform.feature.review.enums;

/**
 * Enum representing the type of key phrase extracted from a review
 */
public enum PhraseType {
    /**
     * Positive aspect or advantage (pros)
     */
    PRO("Điểm cộng"),
    
    /**
     * Negative aspect or disadvantage (cons)
     */
    CON("Điểm trừ"),
    
    /**
     * Neutral information
     */
    NEUTRAL("Trung lập"),
    
    /**
     * General aspect or topic mentioned
     */
    ASPECT("Khía cạnh");

    private final String displayName;

    PhraseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
