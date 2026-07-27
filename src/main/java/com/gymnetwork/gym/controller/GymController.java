package com.gymnetwork.gym.controller;

import com.gymnetwork.common.dto.ApiResponse;
import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.gym.dto.response.*;
import com.gymnetwork.gym.service.GymService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/gyms")
@RequiredArgsConstructor
@Tag(name = "Gym Discovery", description = "Public Gym Discovery & Detail APIs")
public class GymController {

    private final GymService gymService;

    @GetMapping
    @Operation(summary = "Get paginated list of approved gyms")
    public ResponseEntity<ApiResponse<PageResponse<GymResponse>>> getAllGyms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getAllGyms(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed gym information by UUID")
    public ResponseEntity<ApiResponse<GymDetailResponse>> getGymById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymById(id)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search gyms by keyword or city")
    public ResponseEntity<ApiResponse<PageResponse<GymResponse>>> searchGyms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(gymService.searchGyms(query, city, PageRequest.of(page, size))));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get gyms near specific latitude and longitude coordinates")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getNearbyGyms(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getNearbyGyms(latitude, longitude, PageRequest.of(page, size))));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending gyms")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getTrendingGyms() {
        return ResponseEntity.ok(ApiResponse.success(gymService.getTrendingGyms()));
    }

    @GetMapping("/top-rated")
    @Operation(summary = "Get top-rated gyms")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getTopRatedGyms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getTopRatedGyms(PageRequest.of(page, size))));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured gyms")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getFeaturedGyms() {
        return ResponseEntity.ok(ApiResponse.success(gymService.getFeaturedGyms()));
    }

    @GetMapping("/{id}/photos")
    @Operation(summary = "Get gym photos")
    public ResponseEntity<ApiResponse<List<GymImageDto>>> getGymPhotos(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymPhotos(id)));
    }

    @GetMapping("/{id}/facilities")
    @Operation(summary = "Get gym facilities")
    public ResponseEntity<ApiResponse<List<GymFacilityDto>>> getGymFacilities(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymFacilities(id)));
    }

    @GetMapping("/{id}/timings")
    @Operation(summary = "Get gym operating timings")
    public ResponseEntity<ApiResponse<List<GymTimingDto>>> getGymTimings(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymTimings(id)));
    }

    @GetMapping("/{id}/holidays")
    @Operation(summary = "Get gym upcoming holidays")
    public ResponseEntity<ApiResponse<List<GymHolidayDto>>> getGymHolidays(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymHolidays(id)));
    }

    @GetMapping("/{id}/occupancy")
    @Operation(summary = "Get live occupancy count and capacity for a gym")
    public ResponseEntity<ApiResponse<GymOccupancyResponse>> getGymOccupancy(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(gymService.getGymOccupancy(id)));
    }
}
