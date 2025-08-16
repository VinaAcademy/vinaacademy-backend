package com.vinaacademy.platform.feature.course.event.listener;

import com.vinaacademy.platform.configuration.AppConfig;
import com.vinaacademy.platform.feature.course.event.CourseStatusChangedEvent;
import com.vinaacademy.platform.feature.course.event.CourseSubmittedForReviewEvent;
import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for course-related domain events.
 * Handles notifications and other side effects.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CourseEventListener {

    private final NotificationService notificationService;

    /**
     * Handle course status changed events by sending notifications to instructors
     */
    @EventListener
    @Async
    public void handleCourseStatusChanged(CourseStatusChangedEvent event) {
        try {
            log.debug("Handling course status changed event for course: {} from {} to {}",
                    event.getCourseId(), event.getPreviousStatus(), event.getNewStatus());

            if (event.getOwner() != null) {
                sendStatusChangeNotification(event);
            }

            log.info("Successfully handled course status changed event for course: {}", event.getCourseId());
        } catch (Exception e) {
            log.error("Failed to handle course status changed event for course: {}", event.getCourseId(), e);
            // Don't rethrow as notification failure should not break the main operation
        }
    }

    /**
     * Handle course submitted for review events by sending notifications to staff/admin
     */
    @EventListener
    @Async
    public void handleCourseSubmittedForReview(CourseSubmittedForReviewEvent event) {
        try {
            log.debug("Handling course submitted for review event for course: {}", event.getCourseId());

            sendSubmissionNotification(event);

            log.info("Successfully handled course submitted for review event for course: {}", event.getCourseId());
        } catch (Exception e) {
            log.error("Failed to handle course submitted for review event for course: {}", event.getCourseId(), e);
            // Don't rethrow as notification failure should not break the main operation
        }
    }

    private void sendStatusChangeNotification(CourseStatusChangedEvent event) {
        String title = "course.notification.status_update.title";
        String content = String.format("course.notification.status_update.content.%s",
                event.getNewStatus().name().toLowerCase());

        String url = String.format("%s/instructor/courses/%s/content",
                AppConfig.INSTANCE.getFrontendUrl(),
                event.getCourseId());

        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .title(title)
                .content(content)
                .targetUrl(url)
                .userId(event.getOwner())
                .type(NotificationType.COURSE_APPROVAL)
                .build();

        notificationService.createNotification(notification);
        log.debug("Status change notification sent to instructor: {} for course: {}",
                event.getOwner(), event.getCourseId());
    }

    private void sendSubmissionNotification(CourseSubmittedForReviewEvent event) {
        // This could be enhanced to send notifications to all staff/admin users
        // For now, we just log the event
        log.info("Course {} ({}) submitted for review by instructor: {}",
                event.getCourseName(), event.getCourseId(), event.getInstructorId());

        // TODO: Implement notification to staff/admin users about course submission
        // This could query for all users with STAFF/ADMIN roles and send notifications
    }
}
