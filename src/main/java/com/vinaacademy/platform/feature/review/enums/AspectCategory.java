package com.vinaacademy.platform.feature.review.enums;

/**
 * Enum representing different aspects/categories that can be mentioned in a review
 */
public enum AspectCategory {
    /**
     * Course content and curriculum
     */
    CONTENT("Nội dung khóa học"),
    
    /**
     * Instructor quality and teaching style
     */
    INSTRUCTOR("Giảng viên"),
    
    /**
     * Technical aspects (platform, video quality, etc.)
     */
    TECHNICAL("Kỹ thuật"),
    
    /**
     * Course support and assistance
     */
    SUPPORT("Hỗ trợ"),
    
    /**
     * Course difficulty level
     */
    DIFFICULTY("Độ khó"),
    
    /**
     * Course pacing and structure
     */
    PACING("Tiến độ"),
    
    /**
     * Course materials and resources
     */
    MATERIALS("Tài liệu"),
    
    /**
     * Price and value for money
     */
    VALUE("Giá trị"),
    
    /**
     * General or uncategorized
     */
    GENERAL("Chung");

    private final String displayName;

    AspectCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
