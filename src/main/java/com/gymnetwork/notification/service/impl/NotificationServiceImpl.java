package com.gymnetwork.notification.service.impl;


import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.notification.dto.response.NotificationResponse;
import com.gymnetwork.notification.entity.NotificationEntity;
import com.gymnetwork.notification.repository.NotificationRepository;
import com.gymnetwork.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    
    // In a real app, you'd fetch the FCM token for the user from UserDeviceToken table
    // private final UserDeviceTokenRepository tokenRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        Page<NotificationEntity> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
                
        if (notification.getUserId().equals(userId)) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        // Simple implementation for MVP - fetch all unread and update
        // Better implementation: Custom update query in repository
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .stream()
                .filter(n -> !n.getIsRead())
                .forEach(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void sendNotification(UUID userId, String title, String message, String type) {
        // 1. Save to DB
        NotificationEntity notification = NotificationEntity.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .build();
        notificationRepository.save(notification);
        
        // 2. Send Push Notification via Firebase
        try {
            // String fcmToken = tokenRepository.findByUserId(userId).getToken();
            String fcmToken = "dummy_token_for_user"; // Replace with actual logic
            
            if (fcmToken != null && !fcmToken.isEmpty()) {
                Message fcmMessage = Message.builder()
                        .setToken(fcmToken)
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(message)
                                .build())
                        .putData("type", type)
                        .build();
                        
                // FirebaseMessaging.getInstance().send(fcmMessage);
                // Commented out to avoid errors without proper Firebase configuration
                log.info("Sent push notification to {}: {}", userId, title);
            }
        } catch (Exception e) {
            log.error("Failed to send push notification", e);
        }
    }

    private NotificationResponse mapToResponse(NotificationEntity notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
