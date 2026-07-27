package com.gymnetwork.gym.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymDetailResponse {
    private GymResponse gymInfo;
    private List<GymImageDto> photos;
    private List<GymFacilityDto> facilities;
    private List<GymTimingDto> timings;
    private List<GymHolidayDto> holidays;
    private List<GymPricingDto> pricing;
    private List<String> badges;
}
