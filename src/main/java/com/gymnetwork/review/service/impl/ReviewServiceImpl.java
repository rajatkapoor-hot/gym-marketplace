package com.gymnetwork.review.service.impl;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.BadRequestException;
import com.gymnetwork.review.dto.request.ReviewRequest;
import com.gymnetwork.review.dto.response.ReviewResponse;
import com.gymnetwork.review.entity.ReviewEntity;
import com.gymnetwork.review.repository.ReviewRepository;
import com.gymnetwork.review.service.ReviewService;
import com.gymnetwork.shared.event.ReviewCreatedEvent;
import com.gymnetwork.shared.service.CheckInInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CheckInInternalService checkInInternalService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse addReview(UUID userId, ReviewRequest request) {
        if (!checkInInternalService.hasUserCheckedInToGym(userId, request.getGymId())) {
            throw new BadRequestException("You can only review gyms you have checked into.");
        }
        
        if (reviewRepository.existsByUserIdAndGymId(userId, request.getGymId())) {
            throw new BadRequestException("You have already reviewed this gym.");
        }
        
        ReviewEntity review = ReviewEntity.builder()
                .userId(userId)
                .gymId(request.getGymId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
                
        review = reviewRepository.save(review);
        
        Double newAverage = reviewRepository.getAverageRatingByGymId(request.getGymId());
        
        eventPublisher.publishEvent(ReviewCreatedEvent.builder()
                .gymId(request.getGymId())
                .newAverageRating(newAverage)
                .build());
                
        return mapToResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getGymReviews(UUID gymId, Pageable pageable) {
        Page<ReviewEntity> page = reviewRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(UUID gymId) {
        return reviewRepository.getAverageRatingByGymId(gymId);
    }

    private ReviewResponse mapToResponse(ReviewEntity review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .gymId(review.getGymId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
