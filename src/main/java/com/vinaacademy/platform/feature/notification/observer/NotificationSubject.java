package com.vinaacademy.platform.feature.notification.observer;

import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;

public interface NotificationSubject {
    void addObserver(NotificationObserver observer);
    void removeObserver(NotificationObserver observer);
    void notifyObservers(NotificationDTO notification, String action);
}