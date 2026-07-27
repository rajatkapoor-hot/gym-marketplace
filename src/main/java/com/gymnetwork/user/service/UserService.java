package com.gymnetwork.user.service;

import com.gymnetwork.user.dto.request.UpdateUserProfileRequest;
import com.gymnetwork.user.dto.response.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserProfileResponse getProfile(UUID userId);
    UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request);
    void deleteProfile(UUID userId);
    List<UUID> getFavourites(UUID userId);
    void addFavourite(UUID userId, UUID gymId);
    void removeFavourite(UUID userId, UUID gymId);
    List<?> getUserBookings(UUID userId);
    List<?> getUserHistory(UUID userId);
}
