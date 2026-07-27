package com.gymnetwork.review.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.review.dto.request.ReviewRequest;
import com.gymnetwork.review.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {
    ReviewResponse addReview(UUID userId, ReviewRequest request);
    PageResponse<ReviewResponse> getGymReviews(UUID gymId, Pageable pageable);
    Double getAverageRating(UUID gymId);
}
