package com.gymnetwork.gym.service;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.gym.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GymService {
    PageResponse<GymResponse> getAllGyms(Pageable pageable);
    GymDetailResponse getGymById(UUID id);
    PageResponse<GymResponse> searchGyms(String query, String city, Pageable pageable);
    List<GymResponse> getNearbyGyms(double latitude, double longitude, Pageable pageable);
    List<GymResponse> getTrendingGyms();
    List<GymResponse> getTopRatedGyms(Pageable pageable);
    List<GymResponse> getFeaturedGyms();
    List<GymImageDto> getGymPhotos(UUID gymId);
    List<GymFacilityDto> getGymFacilities(UUID gymId);
    List<GymTimingDto> getGymTimings(UUID gymId);
    List<GymHolidayDto> getGymHolidays(UUID gymId);
    GymOccupancyResponse getGymOccupancy(UUID gymId);
}
