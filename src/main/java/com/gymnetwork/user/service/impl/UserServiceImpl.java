package com.gymnetwork.user.service.impl;

import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.shared.service.BookingInternalService;
import com.gymnetwork.user.dto.request.UpdateUserProfileRequest;
import com.gymnetwork.user.dto.response.UserProfileResponse;
import com.gymnetwork.user.entity.UserEntity;
import com.gymnetwork.user.entity.UserFavouriteEntity;
import com.gymnetwork.user.entity.UserProfileEntity;
import com.gymnetwork.user.repository.UserFavouriteRepository;
import com.gymnetwork.user.repository.UserProfileRepository;
import com.gymnetwork.user.repository.UserRepository;
import com.gymnetwork.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserFavouriteRepository userFavouriteRepository;
    private final BookingInternalService bookingInternalService;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        return mapToResponse(user, profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateUserProfileRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getProfilePictureUrl() != null) profile.setProfilePictureUrl(request.getProfilePictureUrl());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getEmergencyContactPhone() != null) profile.setEmergencyContactPhone(request.getEmergencyContactPhone());

        userProfileRepository.save(profile);
        return mapToResponse(user, profile);
    }

    @Override
    @Transactional
    public void deleteProfile(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.markDeleted();
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> getFavourites(UUID userId) {
        return userFavouriteRepository.findByUserId(userId).stream()
                .map(UserFavouriteEntity::getGymId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addFavourite(UUID userId, UUID gymId) {
        if (!userFavouriteRepository.existsByUserIdAndGymId(userId, gymId)) {
            UserFavouriteEntity favourite = UserFavouriteEntity.builder()
                    .userId(userId)
                    .gymId(gymId)
                    .build();
            userFavouriteRepository.save(favourite);
        }
    }

    @Override
    @Transactional
    public void removeFavourite(UUID userId, UUID gymId) {
        userFavouriteRepository.deleteByUserIdAndGymId(userId, gymId);
    }

    @Override
    public List<?> getUserBookings(UUID userId) {
        return bookingInternalService.getUserBookings(userId);
    }

    @Override
    public List<?> getUserHistory(UUID userId) {
        return bookingInternalService.getUserBookingHistory(userId);
    }

    private UserProfileResponse mapToResponse(UserEntity user, UserProfileEntity profile) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .gender(profile.getGender())
                .dateOfBirth(profile.getDateOfBirth())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .build();
    }
}
