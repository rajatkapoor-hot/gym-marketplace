package com.gymnetwork.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class GymAnalyticsResponse {
    private UUID gymId;
    private Long totalCheckIns;
    private BigDecimal totalEarnings;
    private Long totalReviews;
    private Double averageRating;
}
