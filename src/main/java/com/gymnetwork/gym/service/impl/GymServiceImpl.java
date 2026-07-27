package com.gymnetwork.gym.service.impl;

import com.gymnetwork.common.dto.PageResponse;
import com.gymnetwork.common.exception.ResourceNotFoundException;
import com.gymnetwork.gym.dto.response.*;
import com.gymnetwork.gym.entity.*;
import com.gymnetwork.gym.repository.*;
import com.gymnetwork.gym.service.GymService;
import com.gymnetwork.gym.specification.GymSpecification;
import com.gymnetwork.shared.enums.GymStatus;
import com.gymnetwork.shared.service.GymInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GymServiceImpl implements GymService, GymInternalService {

    private final GymRepository gymRepository;
    private final GymImageRepository gymImageRepository;
    private final GymFacilityRepository gymFacilityRepository;
    private final GymTimingRepository gymTimingRepository;
    private final GymHolidayRepository gymHolidayRepository;
    private final GymPricingRepository gymPricingRepository;
    private final GymBadgeRepository gymBadgeRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GymResponse> getAllGyms(Pageable pageable) {
        Page<GymEntity> page = gymRepository.findByStatus(GymStatus.APPROVED, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "gym_details", key = "#id")
    public GymDetailResponse getGymById(UUID id) {
        GymEntity gym = gymRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found with ID: " + id));

        return GymDetailResponse.builder()
                .gymInfo(mapToResponse(gym))
                .photos(getGymPhotos(id))
                .facilities(getGymFacilities(id))
                .timings(getGymTimings(id))
                .holidays(getGymHolidays(id))
                .pricing(getGymPricing(id))
                .badges(gymBadgeRepository.findByGymId(id).stream().map(GymBadgeEntity::getBadgeType).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GymResponse> searchGyms(String query, String city, Pageable pageable) {
        Specification<GymEntity> spec = Specification.where(GymSpecification.isApproved())
                .and(GymSpecification.hasCity(city))
                .and(GymSpecification.searchByNameOrAddress(query));
        Page<GymEntity> page = gymRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::mapToResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymResponse> getNearbyGyms(double latitude, double longitude, Pageable pageable) {
        return gymRepository.findNearbyGyms(latitude, longitude, pageable).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymResponse> getTrendingGyms() {
        Pageable pageable = PageRequest.of(0, 10);
        return gymRepository.findByStatusOrderByRatingAverageDesc(GymStatus.APPROVED, pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymResponse> getTopRatedGyms(Pageable pageable) {
        return gymRepository.findByStatusOrderByRatingAverageDesc(GymStatus.APPROVED, pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymResponse> getFeaturedGyms() {
        Pageable pageable = PageRequest.of(0, 5);
        return gymRepository.findByStatusOrderByRatingAverageDesc(GymStatus.APPROVED, pageable).getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymImageDto> getGymPhotos(UUID gymId) {
        return gymImageRepository.findByGymIdOrderByDisplayOrderAsc(gymId).stream()
                .map(img -> GymImageDto.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isCover(img.getIsCover())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymFacilityDto> getGymFacilities(UUID gymId) {
        return gymFacilityRepository.findByGymId(gymId).stream()
                .map(f -> GymFacilityDto.builder()
                        .id(f.getId())
                        .facilityName(f.getFacilityName())
                        .iconName(f.getIconName())
                        .description(f.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymTimingDto> getGymTimings(UUID gymId) {
        return gymTimingRepository.findByGymId(gymId).stream()
                .map(t -> GymTimingDto.builder()
                        .id(t.getId())
                        .dayOfWeek(t.getDayOfWeek())
                        .openTime(t.getOpenTime())
                        .closeTime(t.getCloseTime())
                        .slotCapacity(t.getSlotCapacity())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymHolidayDto> getGymHolidays(UUID gymId) {
        return gymHolidayRepository.findByGymId(gymId).stream()
                .map(h -> GymHolidayDto.builder()
                        .id(h.getId())
                        .holidayDate(h.getHolidayDate())
                        .reason(h.getReason())
                        .build())
                .collect(Collectors.toList());
    }

    private List<GymPricingDto> getGymPricing(UUID gymId) {
        return gymPricingRepository.findByGymIdAndIsActiveTrue(gymId).stream()
                .map(p -> GymPricingDto.builder()
                        .id(p.getId())
                        .passType(p.getPassType())
                        .originalPrice(p.getOriginalPrice())
                        .discountedPrice(p.getDiscountedPrice())
                        .isActive(p.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GymOccupancyResponse getGymOccupancy(UUID gymId) {
        if (!gymRepository.existsById(gymId)) {
            throw new ResourceNotFoundException("Gym not found");
        }
        int mockOccupancy = 18;
        int maxCapacity = 50;
        double percentage = ((double) mockOccupancy / maxCapacity) * 100;
        return GymOccupancyResponse.builder()
                .gymId(gymId)
                .currentOccupancy(mockOccupancy)
                .maxCapacity(maxCapacity)
                .percentage(percentage)
                .build();
    }

    // GymInternalService methods
    @Override
    public boolean existsById(UUID gymId) {
        return gymRepository.existsById(gymId);
    }

    @Override
    public UUID getGymOwnerId(UUID gymId) {
        return gymRepository.findById(gymId)
                .map(GymEntity::getOwnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));
    }

    @Override
    public BigDecimal getGymCommissionRate(UUID gymId) {
        return gymRepository.findById(gymId)
                .map(GymEntity::getCommissionRate)
                .orElse(new BigDecimal("10.00"));
    }

    @Override
    @Transactional
    public void updateGymRating(UUID gymId, int newRating) {
        GymEntity gym = gymRepository.findById(gymId)
                .orElseThrow(() -> new ResourceNotFoundException("Gym not found"));

        int currentCount = gym.getRatingCount() != null ? gym.getRatingCount() : 0;
        BigDecimal currentAvg = gym.getRatingAverage() != null ? gym.getRatingAverage() : BigDecimal.ZERO;

        BigDecimal totalSum = currentAvg.multiply(BigDecimal.valueOf(currentCount)).add(BigDecimal.valueOf(newRating));
        int updatedCount = currentCount + 1;
        BigDecimal updatedAvg = totalSum.divide(BigDecimal.valueOf(updatedCount), 2, RoundingMode.HALF_UP);

        gym.setRatingCount(updatedCount);
        gym.setRatingAverage(updatedAvg);
        gymRepository.save(gym);
    }

    private GymResponse mapToResponse(GymEntity gym) {
        String coverImage = gymImageRepository.findByGymIdOrderByDisplayOrderAsc(gym.getId()).stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsCover()))
                .findFirst()
                .map(GymImageEntity::getImageUrl)
                .orElse(null);

        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .slug(gym.getSlug())
                .description(gym.getDescription())
                .addressLine1(gym.getAddressLine1())
                .city(gym.getCity())
                .state(gym.getState())
                .pincode(gym.getPincode())
                .latitude(gym.getLatitude())
                .longitude(gym.getLongitude())
                .status(gym.getStatus().name())
                .ratingAverage(gym.getRatingAverage())
                .ratingCount(gym.getRatingCount())
                .coverImageUrl(coverImage)
                .build();
    }
}
