package com.vinaacademy.platform.feature.notification.repository;

import com.vinaacademy.platform.feature.notification.entity.Notification;
import com.vinaacademy.platform.feature.notification.enums.NotificationType;
import com.vinaacademy.platform.feature.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUser(User user, Pageable pageable);

    Page<Notification> findByUserAndType(User user, NotificationType type, Pageable pageable);

    Page<Notification> findByUserAndIsRead(User user, Boolean isRead, Pageable pageable);

    Page<Notification> findByUserAndTypeAndIsRead(User user, NotificationType type, Boolean isRead, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.user = :user AND n.isRead = false")
    int markUnreadAsRead(User user);

    List<Notification> findByIsReadAndUser(boolean isRead, User user);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE " +
            "n.id IN :notifications AND n.isRead = false")
    int markRead(List<Notification> notifications);

}