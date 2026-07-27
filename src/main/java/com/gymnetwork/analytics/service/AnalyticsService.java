package com.gymnetwork.analytics.service;

import com.gymnetwork.analytics.dto.response.GymAnalyticsResponse;

import java.util.UUID;

public interface AnalyticsService {
    GymAnalyticsResponse getGymAnalytics(UUID gymId);
}
