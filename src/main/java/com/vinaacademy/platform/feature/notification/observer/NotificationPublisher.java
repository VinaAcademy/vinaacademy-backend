package com.vinaacademy.platform.feature.notification.observer;

import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NotificationPublisher implements NotificationSubject {

    private final List<NotificationObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.debug("Observer added: {}", observer.getClass().getSimpleName());
        }
    }

    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
        log.debug("Observer removed: {}", observer.getClass().getSimpleName());
    }

    @Override
    @Async("notificationExecutor")
    @Transactional()
    public void notifyObservers(NotificationDTO notification, String action) {
        for (NotificationObserver observer : observers) {
            try {
                switch (action) {
                    case NotificationAction.CREATE:
                        observer.onNotificationCreated(notification);
                        break;
                    case NotificationAction.READ:
                        observer.onNotificationRead(notification);
                        break;
                    case NotificationAction.DELETE:
                        observer.onNotificationDeleted(notification);
                        break;
                    default:
                        log.warn("Unknown notification action: {}", action);
                }
            } catch (Exception e) {
                log.error("Error notifying observer {}: {}", observer.getClass().getSimpleName(), e.getMessage());
            }
        }
    }
}