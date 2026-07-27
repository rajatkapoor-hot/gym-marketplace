package com.gymnetwork.review.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID userId;
    private String userName; // Usually populated by joining with User profile or fetching via Service
    private UUID gymId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
}
