package com.gymnetwork.analytics.controller;

import com.gymnetwork.analytics.dto.response.GymAnalyticsResponse;
import com.gymnetwork.analytics.service.AnalyticsService;
import com.gymnetwork.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics Management", description = "Gym Owner Analytics APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('ADMIN')")
    @Operation(summary = "Get analytics for a specific gym")
    public ResponseEntity<ApiResponse<GymAnalyticsResponse>> getGymAnalytics(@PathVariable UUID gymId) {
        return ResponseEntity.ok(ApiResponse.success(analyticsService.getGymAnalytics(gymId)));
    }
}
