package com.gymnetwork.notification.listener;


import com.gymnetwork.shared.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    

    @Async
    @EventListener
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        log.info("Received ReviewCreatedEvent for gym {}", event.getGymId());
        // Here we could notify the gym owner that a new review was posted
        // But we need the gym owner's userId. Assuming GymService can provide it,
        // we could fetch it via an internal service or include it in the event.
        // For demonstration, let's just log it.
        
        // Example: 
        // UUID ownerId = gymInternalService.getGymOwnerId(event.getGymId());
        // notificationService.sendNotification(ownerId, "New Review", "Your gym received a new review. New rating: " + event.getNewAverageRating(), "REVIEW");
    }
}
