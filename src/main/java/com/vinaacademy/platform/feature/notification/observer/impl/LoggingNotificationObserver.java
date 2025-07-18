package com.vinaacademy.platform.feature.notification.observer.impl;

import com.vinaacademy.platform.feature.log.service.LogService;
import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import com.vinaacademy.platform.feature.notification.observer.NotificationObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingNotificationObserver implements NotificationObserver {
    
    private final LogService logService;
    
    @Override
    public void onNotificationCreated(NotificationDTO notification) {
        log.debug("Notification created: {}", notification.getId());
        logService.log("Notification", "CREATE", "Notification created", null, notification);
    }
    
    @Override
    public void onNotificationRead(NotificationDTO notification) {
        log.debug("Notification read: {}", notification.getId());
        logService.log("Notification", "READ",
                "Notification marked as read", null, notification);
    }
    
    @Override
    public void onNotificationDeleted(NotificationDTO notification) {
        log.debug("Notification deleted: {}", notification.getId());
        logService.log("Notification", "DELETE",
                "Notification deleted", null, notification);
    }
}