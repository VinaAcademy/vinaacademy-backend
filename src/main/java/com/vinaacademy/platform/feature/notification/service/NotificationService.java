package com.vinaacademy.platform.feature.notification.service;

import com.vinaacademy.platform.feature.notification.dto.NotificationCreateDTO;
import com.vinaacademy.platform.feature.notification.dto.NotificationDTO;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationDTO createNotification(NotificationCreateDTO dto);
    Page<NotificationDTO> getUserNotificationsPaginated(Boolean read, NotificationType type, Pageable pageable);
    void markAsRead(UUID notificationId);
    void deleteNotification(UUID notificationId);
    void markReadAll();
}