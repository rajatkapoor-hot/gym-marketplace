package com.gymnetwork.analytics.service.impl;

import com.gymnetwork.analytics.dto.response.GymAnalyticsResponse;
import com.gymnetwork.analytics.entity.GymAnalyticsEntity;
import com.gymnetwork.analytics.repository.GymAnalyticsRepository;
import com.gymnetwork.analytics.service.AnalyticsService;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.shared.event.CheckInCompletedEvent;
import com.gymnetwork.shared.event.ReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final GymAnalyticsRepository analyticsRepository;

    @Override
    @Transactional(readOnly = true)
    public GymAnalyticsResponse getGymAnalytics(UUID gymId) {
        GymAnalyticsEntity analytics = analyticsRepository.findByGymId(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics not found for gym: " + gymId));
                
        return GymAnalyticsResponse.builder()
                .gymId(analytics.getGymId())
                .totalCheckIns(analytics.getTotalCheckIns())
                .totalEarnings(analytics.getTotalEarnings())
                .totalReviews(analytics.getTotalReviews())
                .averageRating(analytics.getAverageRating())
                .build();
    }

    @Async
    @EventListener
    @Transactional
    public void handleCheckInCompletedEvent(CheckInCompletedEvent event) {
        GymAnalyticsEntity analytics = analyticsRepository.findByGymId(event.getGymId())
                .orElseGet(() -> GymAnalyticsEntity.builder().gymId(event.getGymId()).build());
                
        analytics.setTotalCheckIns(analytics.getTotalCheckIns() + 1);
        
        // Add gym's net earnings (assuming 90% of the amount for simplicity, similar to settlement logic)
        BigDecimal netEarning = event.getAmount().multiply(new BigDecimal("0.90"));
        analytics.setTotalEarnings(analytics.getTotalEarnings().add(netEarning));
        
        analyticsRepository.save(analytics);
    }

    @Async
    @EventListener
    @Transactional
    public void handleReviewCreatedEvent(ReviewCreatedEvent event) {
        GymAnalyticsEntity analytics = analyticsRepository.findByGymId(event.getGymId())
                .orElseGet(() -> GymAnalyticsEntity.builder().gymId(event.getGymId()).build());
                
        analytics.setTotalReviews(analytics.getTotalReviews() + 1);
        analytics.setAverageRating(event.getNewAverageRating());
        
        analyticsRepository.save(analytics);
    }
}
