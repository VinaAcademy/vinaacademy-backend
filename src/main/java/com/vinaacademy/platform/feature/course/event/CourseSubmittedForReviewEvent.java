package com.vinaacademy.platform.feature.course.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a course is submitted for review.
 * This event can be used for notifications to staff/admin for approval.
 */
@Data
@Builder
public class CourseSubmittedForReviewEvent {
    
    /**
     * The ID of the course submitted for review
     */
    private final UUID courseId;
    
    /**
     * The course slug for easy reference
     */
    private final String courseSlug;
    
    /**
     * The course name for notifications
     */
    private final String courseName;
    
    /**
     * The ID of the instructor who submitted the course
     */
    private final UUID instructorId;
    
    /**
     * The timestamp when the submission occurred
     */
    private final LocalDateTime timestamp;
}
