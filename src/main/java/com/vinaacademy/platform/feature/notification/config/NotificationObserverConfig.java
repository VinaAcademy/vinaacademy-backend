package com.vinaacademy.platform.feature.notification.config;

import com.vinaacademy.platform.feature.notification.observer.NotificationPublisher;
import com.vinaacademy.platform.feature.notification.observer.NotificationSubject;
import com.vinaacademy.platform.feature.notification.observer.impl.EmailNotificationObserver;
import com.vinaacademy.platform.feature.notification.observer.impl.LoggingNotificationObserver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationObserverConfig {

    private final NotificationSubject notificationPublisher;
    private final LoggingNotificationObserver loggingObserver;
    private final EmailNotificationObserver emailObserver;

    @PostConstruct
    public void registerObservers() {
        notificationPublisher.addObserver(loggingObserver);
        notificationPublisher.addObserver(emailObserver);
    }
}