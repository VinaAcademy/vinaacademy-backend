package com.vinaacademy.platform.feature.course.projection;

import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Lightweight projection for course list/search operations.
 * Only includes essential fields to improve performance.
 */
public interface CourseListProjection {
    
    UUID getId();
    String getName();
    String getSlug();
    String getDescription();
    String getImage();
    CourseLevel getLevel();
    String getLanguage();
    BigDecimal getPrice();
    Double getRating();
    Integer getTotalStudent();
    Integer getTotalLesson();
    Integer getTotalSection();
    CourseStatus getStatus();
    LocalDateTime getCreatedDate();
    LocalDateTime getUpdatedDate();
    
    // Category info
    String getCategoryName();
    String getCategorySlug();
    
    // Instructor count
    Integer getInstructorCount();
}
