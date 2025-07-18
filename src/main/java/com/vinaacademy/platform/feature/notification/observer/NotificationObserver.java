package com.vinaacademy.platform.feature.notification.observer;

import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;

public interface NotificationObserver {
    void onNotificationCreated(NotificationDTO notification);
    void onNotificationRead(NotificationDTO notification);
    void onNotificationDeleted(NotificationDTO notification);
}