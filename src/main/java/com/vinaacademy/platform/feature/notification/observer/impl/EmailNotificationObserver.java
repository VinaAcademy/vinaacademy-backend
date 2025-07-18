package com.vinaacademy.platform.feature.notification.observer.impl;

import com.vinaacademy.platform.feature.email.service.EmailService;
import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import com.vinaacademy.platform.feature.notification.entity.Notification;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.notification.observer.NotificationObserver;
import com.vinaacademy.platform.feature.notification.repository.NotificationRepository;
import com.vinaacademy.platform.feature.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationObserver implements NotificationObserver {

    private final EmailService emailService;

    @Value("${application.notifications.email.enabled:false}")
    private boolean emailNotificationsEnabled;

    @Override
    public void onNotificationCreated(NotificationDTO notification) {
        // Only send email for specific notification types or based on settings
        if (emailNotificationsEnabled) {
            try {
                String emailContent = notification.getContent();
                String subject = String.format("Thông báo mới: %s - VinaAcademy", notification.getTitle());

                emailService.sendNotificationEmail(notification.getEmail(), subject, emailContent,
                        notification.getTargetUrl(), "Xem thông báo");
            } catch (Exception e) {
                log.error("Failed to send notification email", e);
            }
        }
    }

    @Override
    public void onNotificationRead(NotificationDTO notification) {
        // Usually no need to send emails for read notifications
    }

    @Override
    public void onNotificationDeleted(NotificationDTO notification) {
        // Usually no need to send emails for deleted notifications
    }
}