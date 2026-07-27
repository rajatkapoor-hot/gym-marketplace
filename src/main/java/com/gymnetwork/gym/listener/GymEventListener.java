package com.gymnetwork.gym.listener;


import com.gymnetwork.gym.repository.GymRepository;
import com.gymnetwork.shared.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class GymEventListener {

    private final GymRepository gymRepository;

    @Async
    @EventListener
    @Transactional
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        log.info("Updating gym rating for gym {}", event.getGymId());
        
        gymRepository.findById(event.getGymId()).ifPresent(gym -> {
            gym.setRatingAverage(BigDecimal.valueOf(event.getNewAverageRating()));
            // gym.setTotalReviews(event.getTotalReviews()); // if tracked
            gymRepository.save(gym);
        });
    }
}
