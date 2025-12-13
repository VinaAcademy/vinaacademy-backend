package com.vinaacademy.platform.feature.course.event;

import com.vinaacademy.platform.feature.category.Category;
import com.vinaacademy.platform.feature.course.entity.Course;
import com.vinaacademy.platform.feature.course.enums.CourseLevel;
import com.vinaacademy.platform.feature.course.enums.CourseStatus;
import com.vinaacademy.platform.feature.enrollment.Enrollment;
import com.vinaacademy.platform.feature.instructor.CourseInstructor;
import com.vinaacademy.platform.feature.review.entity.CourseReview;
import com.vinaacademy.platform.feature.section.entity.Section;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain event published when course status changes.
 * This event can be used for notifications, audit trails, webhooks, etc.
 */

@Builder
@Data
@AllArgsConstructor
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
    
    private final String content;
}
