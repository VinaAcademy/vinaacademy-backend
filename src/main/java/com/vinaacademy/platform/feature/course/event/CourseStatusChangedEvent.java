package com.vinaacademy.platform.feature.course.event;

import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when course status changes.
 * This event can be used for notifications, audit trails, webhooks, etc.
 */
@Data
@Builder
public class CourseStatusChangedEvent {

    /**
     * The ID of the course whose status changed
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
     * The previous status of the course
     */
    private final CourseStatus previousStatus;

    /**
     * The new status of the course
     */
    private final CourseStatus newStatus;

    /**
     * The ID of the user who triggered the status change
     */
    private final UUID actorId;

    /**
     * The timestamp when the status change occurred
     */
    private final LocalDateTime timestamp;

    /**
     * The first instructor ID (for notifications)
     */
    private final UUID owner;
}
