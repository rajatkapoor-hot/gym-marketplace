package com.gymnetwork.notification.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    PageResponse<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable);
    void markAsRead(UUID userId, UUID notificationId);
    void markAllAsRead(UUID userId);
    long getUnreadCount(UUID userId);
    
    // Internal methods used by event listeners
    void sendNotification(UUID userId, String title, String message, String type);
}
